package com.uzicus.glplayersample.processing.effects

import android.content.Context
import com.uzicus.glplayersample.processing.effects.shader.BaseShader
import com.uzicus.glplayersample.utils.loadAsString

class TranslucentShader(context: Context): BaseShader() {

    override val adjustAspect = 0.5F

    override val vertexShaderCode: String = context.assets.loadAsString("base.vert")

    override val fragmentShaderCode: String = context.assets.loadAsString("translucent.frag")
}