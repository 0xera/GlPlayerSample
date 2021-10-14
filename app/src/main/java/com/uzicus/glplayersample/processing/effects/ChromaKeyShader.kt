package com.uzicus.glplayersample.processing.effects

import android.content.Context
import com.uzicus.glplayersample.processing.effects.shader.BaseShader
import com.uzicus.glplayersample.utils.loadAsString

class ChromaKeyShader(context: Context) : BaseShader() {

    override val vertexShaderCode: String = context.assets.loadAsString("base.vert")

    override val fragmentShaderCode: String = context.assets.loadAsString("chroma_key.frag")

}