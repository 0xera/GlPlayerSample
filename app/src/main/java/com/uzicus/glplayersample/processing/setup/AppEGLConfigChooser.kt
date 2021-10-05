package com.uzicus.glplayersample.processing.setup

import android.opengl.GLSurfaceView.EGLConfigChooser
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

class AppEGLConfigChooser @JvmOverloads constructor(
    private val redSize: Int = 8,
    private val greenSize: Int = 8,
    private val blueSize: Int = 8,
    private val alphaSize: Int = 0,
    private val depthSize: Int = 0,
    private val stencilSize: Int = 0,
    version: Int = EGL_CONTEXT_CLIENT_VERSION,
) : EGLConfigChooser {

    companion object {
        const val EGL_CONTEXT_CLIENT_VERSION = 2
        private const val EGL_OPENGL_ES2_BIT = 4
    }

    private val configSpec: IntArray = filterConfigSpec(
        intArrayOf(
            EGL10.EGL_RED_SIZE, redSize,
            EGL10.EGL_GREEN_SIZE, greenSize,
            EGL10.EGL_BLUE_SIZE, blueSize,
            EGL10.EGL_ALPHA_SIZE, alphaSize,
            EGL10.EGL_DEPTH_SIZE, depthSize,
            EGL10.EGL_STENCIL_SIZE, stencilSize,
            EGL10.EGL_NONE
        ), version
    )

    private fun filterConfigSpec(configSpec: IntArray, version: Int): IntArray {
        if (version != 2) {
            return configSpec
        }
        val len = configSpec.size
        val newConfigSpec = IntArray(len + 2)
        System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1)
        newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE
        newConfigSpec[len] = EGL_OPENGL_ES2_BIT
        newConfigSpec[len + 1] = EGL10.EGL_NONE
        return newConfigSpec
    }

    //////////////////////////////////////////////////////////////////////////
    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
        val num_config = IntArray(1)
        require(
            egl.eglChooseConfig(
                display,
                configSpec,
                null,
                0,
                num_config
            )
        ) { "eglChooseConfig failed" }

        val config_size = num_config[0]
        require(config_size > 0) { "No configs match configSpec" }

        val configs = arrayOfNulls<EGLConfig>(config_size)
        require(
            egl.eglChooseConfig(
                display,
                configSpec,
                configs,
                config_size,
                num_config
            )
        ) { "eglChooseConfig#2 failed" }

        return chooseConfig(egl, display, configs)
            ?: throw IllegalArgumentException("No config chosen")
    }

    private fun chooseConfig(
        egl: EGL10,
        display: EGLDisplay,
        configs: Array<EGLConfig?>,
    ): EGLConfig? {
        for (config in configs) {
            val d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0)
            val s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0)
            if (d >= depthSize && s >= stencilSize) {
                val r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0)
                val g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0)
                val b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0)
                val a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0)
                if (r == redSize && g == greenSize && b == blueSize && a == alphaSize) {
                    return config
                }
            }
        }
        return null
    }

    private fun findConfigAttrib(
        egl: EGL10,
        display: EGLDisplay,
        config: EGLConfig?,
        attribute: Int,
        defaultValue: Int,
    ): Int {
        val value = IntArray(1)
        return if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
            value[0]
        } else defaultValue
    }

}