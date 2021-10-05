package com.uzicus.glplayersample.processing.effects

import android.content.Context
import android.graphics.PixelFormat
import com.uzicus.glplayersample.utils.loadAsString

class TranslucentVideoEffect(
    context: Context
): BaseSimpleVideoEffect(context) {

    override val pixelFormat = PixelFormat.RGBA_8888

    override val fragmentShaderCode: String = context.assets.loadAsString("translucent.frag")
}