package com.alexmercerind.media_kit_video;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

final class HdrEglCore {
    static final int EGL_RECORDABLE_ANDROID = 0x3142;
    static final int EGL_GL_COLORSPACE_KHR = 0x309D;
    static final int EGL_GL_COLORSPACE_BT2020_PQ_EXT = 0x3340;

    final EGLDisplay display;
    final EGLContext context;
    final EGLConfig config;

    HdrEglCore() {
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (display == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("Unable to get EGL14 display");
        }
        final int[] versions = new int[2];
        if (!EGL14.eglInitialize(display, versions, 0, versions, 1)) {
            throw new RuntimeException("Unable to initialize EGL14");
        }
        EGLConfig candidate = chooseConfig(true);
        if (candidate == null) candidate = chooseConfig(false);
        if (candidate == null) throw new RuntimeException("Unable to find EGL config");
        config = candidate;
        context = EGL14.eglCreateContext(
                display,
                config,
                EGL14.EGL_NO_CONTEXT,
                new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE},
                0
        );
        if (context == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("Failed to create EGL context");
        }
    }

    private EGLConfig chooseConfig(boolean recordable) {
        final int[] attrs = recordable
                ? new int[]{
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL_RECORDABLE_ANDROID, 1,
                    EGL14.EGL_NONE
                }
                : new int[]{
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_NONE
                };
        final EGLConfig[] configs = new EGLConfig[1];
        final int[] count = new int[1];
        if (!EGL14.eglChooseConfig(display, attrs, 0, configs, 0, 1, count, 0) || count[0] <= 0) {
            return null;
        }
        return configs[0];
    }

    EGLSurface createWindowSurface(Object surface, boolean hdr) {
        final int[] attrs = hdr
                ? new int[]{EGL_GL_COLORSPACE_KHR, EGL_GL_COLORSPACE_BT2020_PQ_EXT, EGL14.EGL_NONE}
                : new int[]{EGL14.EGL_NONE};
        final EGLSurface result = EGL14.eglCreateWindowSurface(display, config, surface, attrs, 0);
        if (result == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("Failed to create window surface, eglError=0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        return result;
    }

    void makeCurrent(EGLSurface surface) {
        if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    void releaseSurface(EGLSurface surface) {
        if (surface != null && surface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(display, surface);
        }
    }

    void release() {
        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(display, context);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(display);
    }
}
