package com.uzicus.glplayersample.player

import com.uzicus.glplayersample.GLTextureView
import com.uzicus.glplayersample.processing.effects.shader.Shader

interface PlayerController {

    fun play(url: String)

    fun pauseOrResume()

    fun attachGlTextureView(glTextureView: GLTextureView)

    fun applyShader(shader: Shader?)

}