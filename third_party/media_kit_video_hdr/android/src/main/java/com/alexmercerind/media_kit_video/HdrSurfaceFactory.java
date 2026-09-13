package com.alexmercerind.media_kit_video;

import android.content.Context;

import java.util.Map;

import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

final class HdrSurfaceFactory extends PlatformViewFactory {
    private final MediaKitVideoPlugin plugin;

    HdrSurfaceFactory(MediaKitVideoPlugin plugin) {
        super(StandardMessageCodec.INSTANCE);
        this.plugin = plugin;
    }

    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        long handle = 0L;
        if (args instanceof Map) {
            final Object value = ((Map<?, ?>) args).get("handle");
            if (value instanceof Number) {
                handle = ((Number) value).longValue();
            } else if (value instanceof String) {
                try { handle = Long.parseLong((String) value); } catch (NumberFormatException ignored) {}
            }
        }
        return new HdrSurfacePlatformView(context, handle, plugin);
    }
}
