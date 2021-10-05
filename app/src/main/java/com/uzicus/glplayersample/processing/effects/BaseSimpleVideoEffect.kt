package com.uzicus.glplayersample.processing.effects

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES20
import androidx.annotation.CallSuper
import com.google.android.exoplayer2.util.GlUtil
import com.uzicus.glplayersample.utils.EglUtils
import com.uzicus.glplayersample.utils.loadAsString

abstract class BaseSimpleVideoEffect(
    context: Context
): VideoEffect {

    companion object {
        private const val ATTRIBUTE_POSITION_NAME = "aPosition"
        private const val ATTRIBUTE_TEXTURE_COORDINATE_NAME = "aTextureCoord"

        private const val UNIFORM_FRAME_TEXTURE_SAMPLER_NAME = "uFrameTexture"
    }

    private val vertexShaderCode: String = context.assets.loadAsString("base.vert")
    abstract val fragmentShaderCode: String

    private var program = 0
    private var frameTexture = -1
    private var attributes: Array<GlUtil.Attribute>? = null
    private var uniforms: Array<GlUtil.Uniform>? = null

    override val aspectFactor = 1F

    override val pixelFormat = PixelFormat.RGB_888

    override fun initialize() {
        program = GlUtil.compileProgram(vertexShaderCode, fragmentShaderCode)
        attributes = EglUtils.getAttributes(program)
        uniforms = GlUtil.getUniforms(program)
    }

    override fun setSurfaceSize(width: Int, height: Int) {
        // do nothing
    }

    override fun draw(frameTexture: Int, frameTimestampUs: Long) {
        // Run the shader program.
        GLES20.glUseProgram(program)
        this.frameTexture = frameTexture

        for (attribute in attributes.orEmpty()) {
            defineAttribute(attribute)
            attribute.bind()
        }

        for (uniform in uniforms.orEmpty()) {
            defineUniform(uniform)
            uniform.bind()
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,  /* first= */0,  /* count= */4)
        GlUtil.checkGlError()
    }

    @CallSuper
    open fun defineAttribute(attribute: GlUtil.Attribute) {
        when (attribute.name) {
            ATTRIBUTE_POSITION_NAME -> attribute.setBuffer(
                floatArrayOf( // X, Y, Z, W
                    -1.0f, -1.0f, 0.0f, 1.0f,
                    1.0f, -1.0f, 0.0f, 1.0f,
                    -1.0f, 1.0f, 0.0f, 1.0f,
                    1.0f, 1.0f, 0.0f, 1.0f
                ), 4
            )
            ATTRIBUTE_TEXTURE_COORDINATE_NAME -> attribute.setBuffer(
                floatArrayOf( // X, Y
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f
                ), 2
            )
        }
    }

    @CallSuper
    open fun defineUniform(uniform: GlUtil.Uniform) {
        when (uniform.name) {
            UNIFORM_FRAME_TEXTURE_SAMPLER_NAME -> uniform.setSamplerTexId(frameTexture,  /* unit= */0)
        }
    }

}