package com.uzicus.glplayersample.processing.effects

interface VideoEffect {

    val pixelFormat: Int

    fun initialize()

    fun setSurfaceSize(width: Int, height: Int)

    fun draw(frameTexture: Int, frameTimestampUs: Long)

}