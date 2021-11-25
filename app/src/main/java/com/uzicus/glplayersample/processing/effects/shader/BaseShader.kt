package com.uzicus.glplayersample.processing.effects.shader

import android.opengl.GLES20
import androidx.annotation.CallSuper
import com.uzicus.glplayersample.utils.gl.EglUtils
import com.uzicus.glplayersample.utils.gl.GlShaderAttribute
import com.uzicus.glplayersample.utils.gl.GlShaderUniform
import com.uzicus.glplayersample.utils.gl.GlShaderUniform.Companion.bind

abstract class BaseShader: Shader {

    companion object {
        const val ATTRIBUTE_POSITION_NAME = "aPosition"
        const val ATTRIBUTE_TEXTURE_COORDINATE_NAME = "aTextureCoord"

        const val UNIFORM_FRAME_TEXTURE_SAMPLER_NAME = "uFrameTexture"
        const val UNIFORM_RESOLUTION = "uResolution"
    }

    abstract val vertexShaderCode: String
    abstract val fragmentShaderCode: String

    private var program = 0
    private var attributes: Array<GlShaderAttribute>? = null
    private var uniforms: Array<GlShaderUniform>? = null

    private var inputTexture = -1
    private var textureWidth: Int = -1
    private var textureHeight: Int = -1

    override val adjustAspect = 1F

    override fun initialize() {
        program = EglUtils.compileProgram(vertexShaderCode, fragmentShaderCode)
        attributes = GlShaderAttribute.getAllFromProgram(program)
        uniforms = GlShaderUniform.getAllFromProgram(program)
    }

    override fun setInputTexture(texture: Int, width: Int, height: Int) {
        inputTexture = texture
        textureWidth = width
        textureHeight = height
    }

    override fun draw() {
        GLES20.glUseProgram(program)

        for (attribute in attributes.orEmpty()) {
            defineAttribute(attribute)
            runCatching { attribute.bind() }
        }

        for (uniform in uniforms.orEmpty()) {
            runCatching { defineUniform(uniform) }
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // Run the shader program because GLES20.glUseProgram.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,  /* first= */0,  /* count= */4)
        EglUtils.checkGlError()
    }

    @CallSuper
    open fun defineAttribute(attribute: GlShaderAttribute) {
        when (attribute.name) {
            ATTRIBUTE_POSITION_NAME -> attribute.setBuffer(
                floatArrayOf(
                //    X      Y     Z      W
                    -1.0f, -1.0f, 0.0f, 1.0f,
                     1.0f, -1.0f, 0.0f, 1.0f,
                    -1.0f,  1.0f, 0.0f, 1.0f,
                     1.0f,  1.0f, 0.0f, 1.0f
                ), 4 // ====> vec4
            )
            ATTRIBUTE_TEXTURE_COORDINATE_NAME -> attribute.setBuffer(
                floatArrayOf(
                //    X      Y
                     0.0f,  1.0f,
                     1.0f,  1.0f,
                     0.0f,  0.0f,
                     1.0f,  0.0f
                ), 2 // ====> vec2
            )
        }
    }

    @CallSuper
    open fun defineUniform(uniform: GlShaderUniform) {
        when (uniform.name) {
            UNIFORM_FRAME_TEXTURE_SAMPLER_NAME -> uniform bind GlShaderUniform.Value.GlSampler(inputTexture, 0)
            UNIFORM_RESOLUTION -> uniform bind GlShaderUniform.Value.GlFloatVec2(textureWidth.toFloat(), textureHeight.toFloat())
        }
    }

}