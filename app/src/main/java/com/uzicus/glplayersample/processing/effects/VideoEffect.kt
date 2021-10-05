package com.uzicus.glplayersample.processing.effects

interface VideoEffect {

    val aspectFactor: Float

    val pixelFormat: Int

    fun initialize()

    fun setSurfaceSize(width: Int, height: Int)

    fun draw(frameTexture: Int, frameTimestampUs: Long)

}