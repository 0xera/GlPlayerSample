package com.uzicus.glplayersample.player

import android.opengl.GLSurfaceView
import com.uzicus.glplayersample.processing.effects.shader.Shader

interface PlayerController {

    fun play(url: String)

    fun pauseOrResume()

    fun attachGlSurfaceView(glSurfaceView: GLSurfaceView)

    fun applyShader(shader: Shader?)

}