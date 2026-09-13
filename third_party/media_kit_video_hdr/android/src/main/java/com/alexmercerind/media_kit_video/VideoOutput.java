package com.alexmercerind.media_kit_video;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

public class VideoOutput {
    public long id = 0;
    public long wid = 0;

    private final long handle;
    private final MethodChannel channelReference;
    private final TextureRegistry.SurfaceTextureEntry surfaceTextureEntry;
    private final Method newGlobalObjectRef;
    private final Method deleteGlobalObjectRef;
    private final Object lock = new Object();

    private Surface mpvSurface;
    private Surface fallbackTextureSurface;
    private Surface externalSurface;
    private SdrToHdrRenderer renderer;

    private boolean conversionEnabled;
    private boolean inputHdr;
    private int videoWidth;
    private int videoHeight;
    private int surfaceWidth;
    private int surfaceHeight;
    private float peak = 1000f;
    private float strength = 1f;
    private float saturation = 1f;
    private float highlight = 1f;
    private float preDarken = 0f;
    private float highlightProtect = 0.65f;
    private boolean firstFrameNotified;

    VideoOutput(long handle, MethodChannel channelReference, TextureRegistry textureRegistryReference) {
        this.handle = handle;
        this.channelReference = channelReference;
        try {
            final Class<?> helper = Class.forName("com.alexmercerind.mediakitandroidhelper.MediaKitAndroidHelper");
            newGlobalObjectRef = helper.getDeclaredMethod("newGlobalObjectRef", Object.class);
            deleteGlobalObjectRef = helper.getDeclaredMethod("deleteGlobalObjectRef", long.class);
            newGlobalObjectRef.setAccessible(true);
            deleteGlobalObjectRef.setAccessible(true);
        } catch (Throwable e) {
            Log.e("media_kit", "media_kit_libs_android_video helper missing", e);
            throw new RuntimeException("Failed to initialize VideoOutput", e);
        }
        surfaceTextureEntry = textureRegistryReference.createSurfaceTexture();
        id = surfaceTextureEntry.id();
        surfaceTextureEntry.surfaceTexture().setOnFrameAvailableListener(texture -> notifyFirstFrame(), new Handler(Looper.getMainLooper()));
        Log.i("media_kit", String.format(Locale.ENGLISH, "VideoOutput: id=%d handle=%d", id, handle));
    }

    private boolean shouldConvert() {
        return conversionEnabled && !inputHdr;
    }

    private boolean valid(Surface surface) {
        try { return surface != null && surface.isValid(); } catch (Throwable ignored) { return false; }
    }

    private Surface fallbackSurface() {
        if (!valid(fallbackTextureSurface)) {
            fallbackTextureSurface = new Surface(surfaceTextureEntry.surfaceTexture());
        }
        return fallbackTextureSurface;
    }

    private Surface directOutputSurface() {
        return valid(externalSurface) ? externalSurface : fallbackSurface();
    }

    private void notifyFirstFrame() {
        synchronized (lock) {
            if (firstFrameNotified) return;
            firstFrameNotified = true;
            final HashMap<String, Object> data = new HashMap<>();
            data.put("handle", handle);
            channelReference.invokeMethod("VideoOutput.WaitUntilFirstFrameRenderedNotify", data);
        }
    }

    private void stopRenderer() {
        final SdrToHdrRenderer active = renderer;
        renderer = null;
        if (active != null) {
            active.shutdown();
            try { active.join(1000L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    private void releaseMpvReference() {
        if (wid != 0) {
            try { deleteGlobalObjectRef.invoke(null, wid); } catch (Throwable e) { Log.w("media_kit_hdr", "deleteGlobalObjectRef failed", e); }
            wid = 0;
        }
        if (mpvSurface != null) {
            // SurfaceHolder owns externalSurface; renderer owns its input surface.
            if (mpvSurface != externalSurface && renderer == null && mpvSurface != fallbackTextureSurface) {
                try { mpvSurface.release(); } catch (Throwable ignored) {}
            }
            mpvSurface = null;
        }
    }

    private void clearFallbackSurface() {
        if (!valid(fallbackTextureSurface)) return;
        try {
            final Canvas canvas = fallbackTextureSurface.lockCanvas(null);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            fallbackTextureSurface.unlockCanvasAndPost(canvas);
        } catch (Throwable ignored) {}
    }

    public long createSurface() {
        synchronized (lock) {
            clearFallbackSurface();
            releaseMpvReference();
            stopRenderer();
            try {
                if (shouldConvert() && valid(externalSurface)) {
                    renderer = new SdrToHdrRenderer(externalSurface);
                    final int w = videoWidth > 0 ? videoWidth : surfaceWidth;
                    final int h = videoHeight > 0 ? videoHeight : surfaceHeight;
                    if (w > 0 && h > 0) renderer.setSize(w, h);
                    renderer.setToneMap(peak, strength, saturation, highlight, preDarken, highlightProtect);
                    renderer.start();
                    mpvSurface = renderer.awaitInputSurface();
                    if (mpvSurface == null) {
                        Log.w("media_kit_hdr", "HDR renderer failed to expose input surface; falling back to direct output");
                        stopRenderer();
                        mpvSurface = directOutputSurface();
                    } else {
                        notifyFirstFrame();
                    }
                } else {
                    mpvSurface = directOutputSurface();
                    if (valid(externalSurface)) notifyFirstFrame();
                }
                wid = (long) newGlobalObjectRef.invoke(null, mpvSurface);
            } catch (Throwable e) {
                Log.e("media_kit_hdr", "createSurface failed", e);
            }
            return wid;
        }
    }

    public long setHdrConversion(boolean enable, boolean isHdr, int width, int height) {
        synchronized (lock) {
            final boolean oldConvert = shouldConvert();
            conversionEnabled = enable;
            inputHdr = isHdr;
            if (width > 0 && height > 0) setSurfaceTextureSize(width, height);
            final boolean newConvert = shouldConvert();

            if (newConvert && !valid(externalSurface)) {
                // Keep the existing Flutter texture surface until the PlatformView surface is ready.
                return wid;
            }
            final boolean requiresRecreate =
                    wid == 0 || oldConvert != newConvert ||
                    (newConvert && renderer == null) ||
                    (!newConvert && valid(externalSurface) && mpvSurface != externalSurface);
            return requiresRecreate ? createSurface() : wid;
        }
    }

    public void setToneMapOptions(float peak, float strength, float saturation, float highlight, float preDarken, float highlightProtect) {
        synchronized (lock) {
            this.peak = peak;
            this.strength = strength;
            this.saturation = saturation;
            this.highlight = highlight;
            this.preDarken = preDarken;
            this.highlightProtect = highlightProtect;
            if (renderer != null) {
                renderer.setToneMap(peak, strength, saturation, highlight, preDarken, highlightProtect);
            }
        }
    }

    public void setExternalSurface(Surface surface, int width, int height) {
        synchronized (lock) {
            final boolean hadSurface = valid(externalSurface);
            externalSurface = surface;
            if (width > 0) surfaceWidth = width;
            if (height > 0) surfaceHeight = height;
            if (!valid(surface)) {
                stopRenderer();
            } else if (!hadSurface && conversionEnabled) {
                final long newWid = createSurface();
                final HashMap<String, Object> data = new HashMap<>();
                data.put("handle", handle);
                data.put("wid", newWid);
                channelReference.invokeMethod("VideoOutput.HdrSurfaceReady", data);
            }
        }
    }

    public void setSurfaceTextureSize(int width, int height) {
        synchronized (lock) {
            if (width <= 0 || height <= 0) return;
            videoWidth = width;
            videoHeight = height;
            try { surfaceTextureEntry.surfaceTexture().setDefaultBufferSize(width, height); } catch (Throwable e) { Log.w("media_kit", "setDefaultBufferSize failed", e); }
            if (renderer != null) renderer.setSize(width, height);
        }
    }

    public HashMap<String, Object> getHdrStatus() {
        synchronized (lock) {
            final HashMap<String, Object> result = new HashMap<>();
            result.put("enabled", conversionEnabled);
            result.put("inputHdr", inputHdr);
            result.put("surfaceReady", valid(externalSurface));
            result.put("converting", shouldConvert() && renderer != null);
            result.put("pqSurfaceActive", renderer != null && renderer.isHdrSurfaceActive());
            return result;
        }
    }

    public void dispose() {
        synchronized (lock) {
            releaseMpvReference();
            stopRenderer();
            try { if (fallbackTextureSurface != null) fallbackTextureSurface.release(); } catch (Throwable ignored) {}
            fallbackTextureSurface = null;
            externalSurface = null;
            try { surfaceTextureEntry.release(); } catch (Throwable ignored) {}
        }
    }
}
