package com.uzicus.glplayersample.processing.effects

import android.content.Context
import com.uzicus.glplayersample.utils.loadAsString

class TranslucentVideoEffect(
    context: Context
): BaseSimpleVideoEffect(context) {

    override val fragmentShaderCode: String = context.assets.loadAsString("translucent.frag")
}