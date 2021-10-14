package com.uzicus.glplayersample.processing.effects.shader

interface Shader {

    val adjustAspect: Float

    fun initialize()

    fun setInputTexture(texture: Int, width: Int, height: Int)

    fun draw()

}