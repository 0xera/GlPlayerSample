package com.uzicus.glplayersample.processing.effects

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.uzicus.glplayersample.processing.effects.shader.BaseShader
import com.uzicus.glplayersample.utils.gl.EglUtils
import com.uzicus.glplayersample.utils.gl.GlShaderUniform
import com.uzicus.glplayersample.utils.gl.GlShaderUniform.Value.GlSampler
import com.uzicus.glplayersample.utils.gl.GlShaderUniform.Companion.bind
import com.uzicus.glplayersample.utils.loadAsString

class TranslucentOverlayShader(
    context: Context,
    private val overlayBitmap: Bitmap
) : BaseShader() {

    companion object {
        private const val UNIFORM_OVERLAY_TEXTURE = "uOverlayTexture"
    }

    private var overlayTextureId: Int = -1

    override val adjustAspect: Float = 1.0F / 3.0F

    override val vertexShaderCode: String = context.assets.loadAsString("base.vert")
    override val fragmentShaderCode: String = context.assets.loadAsString("translucent_overlay.frag")

    override fun initialize() {
        super.initialize()

        overlayTextureId = EglUtils.createTexture(GLES20.GL_TEXTURE_2D)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, overlayBitmap, 0)
        overlayBitmap.recycle()
    }

    override fun defineUniform(uniform: GlShaderUniform) {
        when (uniform.name) {
            UNIFORM_OVERLAY_TEXTURE -> uniform bind GlSampler(overlayTextureId, 1)
            else -> super.defineUniform(uniform)
        }
    }

}