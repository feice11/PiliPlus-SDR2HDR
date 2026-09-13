package com.alexmercerind.media_kit_video;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import io.flutter.plugin.platform.PlatformView;

final class HdrSurfacePlatformView implements PlatformView, SurfaceHolder.Callback {
    private final SurfaceView surfaceView;
    private final long handle;
    private final MediaKitVideoPlugin plugin;

    HdrSurfacePlatformView(Context context, long handle, MediaKitVideoPlugin plugin) {
        this.handle = handle;
        this.plugin = plugin;
        surfaceView = new SurfaceView(context);
        surfaceView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public View getView() {
        return surfaceView;
    }

    @Override
    public void dispose() {
        surfaceView.getHolder().removeCallback(this);
        plugin.setExternalSurface(handle, null, 0, 0);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        plugin.setExternalSurface(handle, holder.getSurface(), surfaceView.getWidth(), surfaceView.getHeight());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        plugin.setExternalSurface(handle, holder.getSurface(), width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        plugin.setExternalSurface(handle, null, 0, 0);
    }
}
