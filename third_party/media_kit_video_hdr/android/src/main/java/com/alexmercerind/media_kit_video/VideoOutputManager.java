/**
 * This file is a part of media_kit (https://github.com/media-kit/media-kit).
 * <p>
 * Copyright © 2021 & onwards, Hitesh Kumar Saini <saini123hitesh@gmail.com>.
 * All rights reserved.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file.
 */
package com.alexmercerind.media_kit_video;

import android.util.Log;
import android.view.Surface;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

public class VideoOutputManager {
    private final HashMap<Long, VideoOutput> videoOutputs = new HashMap<>();
    private final MethodChannel channelReference;
    private final TextureRegistry textureRegistryReference;
    private final Object lock = new Object();

    VideoOutputManager(MethodChannel channelReference, TextureRegistry textureRegistryReference) {
        this.channelReference = channelReference;
        this.textureRegistryReference = textureRegistryReference;
    }

    public VideoOutput create(long handle) {
        synchronized (lock) {
            Log.i("media_kit", String.format(Locale.ENGLISH, "com.alexmercerind.media_kit_video.VideoOutputManager.create: %d", handle));
            if (!videoOutputs.containsKey(handle)) {
                final VideoOutput videoOutput = new VideoOutput(handle, channelReference, textureRegistryReference);
                videoOutputs.put(handle, videoOutput);
            }
            return videoOutputs.get(handle);
        }
    }

    public void dispose(long handle) {
        synchronized (lock) {
            Log.i("media_kit", String.format(Locale.ENGLISH, "com.alexmercerind.media_kit_video.VideoOutputManager.dispose: %d", handle));
            if (videoOutputs.containsKey(handle)) {
                Objects.requireNonNull(videoOutputs.get(handle)).dispose();
                videoOutputs.remove(handle);
            }
        }
    }

    public long createSurface(long handle) {
        synchronized (lock) {
            Log.i("media_kit", String.format(Locale.ENGLISH, "com.alexmercerind.media_kit_video.VideoOutputManager.createSurface: %d", handle));
            if (videoOutputs.containsKey(handle)) {
                return Objects.requireNonNull(videoOutputs.get(handle)).createSurface();
            }
            return 0;
        }
    }

    public void setSurfaceTextureSize(long handle, int width, int height) {
        synchronized (lock) {
            Log.i("media_kit", String.format(Locale.ENGLISH, "com.alexmercerind.media_kit_video.VideoOutputManager.setSurfaceTextureSize: %d %d %d", handle, width, height));
            if (videoOutputs.containsKey(handle)) {
                Objects.requireNonNull(videoOutputs.get(handle)).setSurfaceTextureSize(width, height);
            }
        }
    }

    public long setHdrConversion(long handle, boolean enable, boolean isHdr, int width, int height) {
        synchronized (lock) {
            if (videoOutputs.containsKey(handle)) {
                return Objects.requireNonNull(videoOutputs.get(handle))
                        .setHdrConversion(enable, isHdr, width, height);
            }
            return 0;
        }
    }

    public void setToneMapOptions(
            long handle,
            float peak,
            float strength,
            float saturation,
            float highlight,
            float preDarken,
            float highlightProtect
    ) {
        synchronized (lock) {
            if (videoOutputs.containsKey(handle)) {
                Objects.requireNonNull(videoOutputs.get(handle)).setToneMapOptions(
                        peak, strength, saturation, highlight, preDarken, highlightProtect
                );
            }
        }
    }

    public void setExternalSurface(long handle, Surface surface, int width, int height) {
        synchronized (lock) {
            if (videoOutputs.containsKey(handle)) {
                Objects.requireNonNull(videoOutputs.get(handle)).setExternalSurface(surface, width, height);
            }
        }
    }

    public HashMap<String, Object> getHdrStatus(long handle) {
        synchronized (lock) {
            if (videoOutputs.containsKey(handle)) {
                return Objects.requireNonNull(videoOutputs.get(handle)).getHdrStatus();
            }
            return new HashMap<>();
        }
    }
}
