package com.alexmercerind.media_kit_video;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class SdrToHdrRenderer extends Thread {
    private static final String TAG = "media_kit_hdr";

    private final Surface outputSurface;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Object frameLock = new Object();
    private final CountDownLatch ready = new CountDownLatch(1);
    private volatile boolean framePending;

    private volatile int outputWidth = 1;
    private volatile int outputHeight = 1;
    private volatile int inputWidth;
    private volatile int inputHeight;

    volatile float peak = 1000f;
    volatile float strength = 1f;
    volatile float saturation = 1f;
    volatile float highlight = 1f;
    volatile float preDarken = 0f;
    volatile float highlightProtect = 0.65f;

    private HdrEglCore egl;
    private EGLSurface eglSurface;
    private SurfaceTexture inputTexture;
    private Surface inputSurface;
    private HandlerThread callbackThread;
    private SdrToHdrProgram program;
    private int oesTexture;
    private int frameIndex;
    private final float[] transform = new float[16];
    private volatile boolean hdrSurfaceActive;

    SdrToHdrRenderer(Surface outputSurface) {
        super("SdrToHdrRenderer");
        this.outputSurface = outputSurface;
    }

    void setSize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        outputWidth = width;
        outputHeight = height;
        inputWidth = width;
        inputHeight = height;
        final SurfaceTexture st = inputTexture;
        if (st != null) {
            try { st.setDefaultBufferSize(width, height); } catch (Throwable ignored) {}
        }
        requestFrame();
    }

    void setToneMap(float peak, float strength, float saturation, float highlight, float preDarken, float highlightProtect) {
        this.peak = peak;
        this.strength = strength;
        this.saturation = saturation;
        this.highlight = highlight;
        this.preDarken = preDarken;
        this.highlightProtect = highlightProtect;
        requestFrame();
    }

    Surface awaitInputSurface() {
        try { ready.await(5000, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        return inputSurface;
    }

    boolean isHdrSurfaceActive() {
        return hdrSurfaceActive;
    }

    void shutdown() {
        running.set(false);
        synchronized (frameLock) {
            frameLock.notifyAll();
        }
    }

    private void requestFrame() {
        synchronized (frameLock) {
            framePending = true;
            frameLock.notifyAll();
        }
    }

    private static int createExternalTexture() {
        final int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        final int id = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, id);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return id;
    }

    @Override
    public void run() {
        try {
            egl = new HdrEglCore();
            try {
                eglSurface = egl.createWindowSurface(outputSurface, true);
                hdrSurfaceActive = true;
                Log.i(TAG, "BT2020_PQ EGL surface active");
            } catch (Throwable hdrFailure) {
                Log.w(TAG, "BT2020_PQ surface failed, fallback to SDR", hdrFailure);
                eglSurface = egl.createWindowSurface(outputSurface, false);
                hdrSurfaceActive = false;
            }
            egl.makeCurrent(eglSurface);
            oesTexture = createExternalTexture();
            inputTexture = new SurfaceTexture(oesTexture);
            final int w = inputWidth > 0 ? inputWidth : outputWidth;
            final int h = inputHeight > 0 ? inputHeight : outputHeight;
            inputTexture.setDefaultBufferSize(Math.max(1, w), Math.max(1, h));
            callbackThread = new HandlerThread("SdrToHdrFrameCb");
            callbackThread.start();
            inputTexture.setOnFrameAvailableListener(surfaceTexture -> requestFrame(), new Handler(callbackThread.getLooper()));
            inputSurface = new Surface(inputTexture);
            program = new SdrToHdrProgram();
            ready.countDown();

            while (running.get()) {
                synchronized (frameLock) {
                    if (!framePending && running.get()) {
                        try { frameLock.wait(250L); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                    }
                    if (!running.get()) break;
                    if (!framePending) continue;
                    framePending = false;
                }
                egl.makeCurrent(eglSurface);
                inputTexture.updateTexImage();
                inputTexture.getTransformMatrix(transform);
                GLES20.glViewport(0, 0, Math.max(1, outputWidth), Math.max(1, outputHeight));
                program.draw(oesTexture, transform, peak, strength, saturation, highlight, preDarken, highlightProtect, frameIndex++);
                if (!EGL14.eglSwapBuffers(egl.display, eglSurface)) {
                    Log.w(TAG, "eglSwapBuffers failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "GL thread failed", e);
            ready.countDown();
        } finally {
            try { if (inputSurface != null) inputSurface.release(); } catch (Throwable ignored) {}
            try { if (inputTexture != null) inputTexture.release(); } catch (Throwable ignored) {}
            try { if (program != null) program.release(); } catch (Throwable ignored) {}
            try {
                if (oesTexture != 0) {
                    final int[] textures = {oesTexture};
                    GLES20.glDeleteTextures(1, textures, 0);
                }
            } catch (Throwable ignored) {}
            try { if (egl != null) egl.releaseSurface(eglSurface); } catch (Throwable ignored) {}
            try { if (egl != null) egl.release(); } catch (Throwable ignored) {}
            try { if (callbackThread != null) callbackThread.quitSafely(); } catch (Throwable ignored) {}
        }
    }
}
