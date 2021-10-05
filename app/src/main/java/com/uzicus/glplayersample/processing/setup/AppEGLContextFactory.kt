package com.uzicus.glplayersample.processing.setup

import android.opengl.GLSurfaceView.EGLContextFactory
import android.util.Log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

class AppEGLContextFactory : EGLContextFactory {

    companion object {
        private const val TAG = "EContextFactory"
        private const val EGL_CONTEXT_CLIENT_VERSION = 0x3098
        private const val EGL_CLIENT_VERSION = 2
    }

    override fun createContext(egl: EGL10, display: EGLDisplay, config: EGLConfig): EGLContext {
        val attrib_list = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, EGL_CLIENT_VERSION, EGL10.EGL_NONE)
        return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, attrib_list)
    }

    override fun destroyContext(egl: EGL10, display: EGLDisplay, context: EGLContext) {
        if (!egl.eglDestroyContext(display, context)) {
            Log.e(TAG, "display:$display context: $context")
            throw RuntimeException("eglDestroyContex" + egl.eglGetError())
        }
    }

}