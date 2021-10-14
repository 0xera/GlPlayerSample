package com.uzicus.glplayersample.utils.gl

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.uzicus.glplayersample.utils.gl.EglUtils.strLen

class GlShaderUniform(program: Int, index: Int) {

    companion object {
        fun getAllFromProgram(program: Int): Array<GlShaderUniform> {
            val uniformCount = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORMS, uniformCount, 0)

            return Array(uniformCount[0]) { index ->
                GlShaderUniform(program, index)
            }
        }

        infix fun GlShaderUniform.bind(value: Value) {
            this.value = value
            bind()
        }
    }

    val name: String
    val type: Int
    val location: Int

    private var value: Value? = null

    init {
        val len = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_UNIFORM_MAX_LENGTH, len, 0)
        val typeBytes = IntArray(1)
        val size = IntArray(1)
        val nameBytes = ByteArray(len[0])
        val ignore = IntArray(1)
        GLES20.glGetActiveUniform(program, index, len[0], ignore, 0, size, 0, typeBytes, 0, nameBytes, 0)

        name = String(nameBytes, 0, nameBytes.strLen)
        location = GLES20.glGetUniformLocation(program, this.name)
        type = typeBytes[0]
    }

    fun bind() {
        value?.bind(location, type)
        EglUtils.checkGlError()
    }

    sealed class Value {
        abstract fun bind(location: Int, type: Int)

        class GlFloat(
            private val value: Float
        ): Value() {
            override fun bind(location: Int, type: Int) {
                check(type == GLES20.GL_FLOAT) { "wrong type" }

                GLES20.glUniform1f(location, value)
            }
        }

        class GlFloatVec2(
            private val first: Float,
            private val second: Float
        ): Value() {
            override fun bind(location: Int, type: Int) {
                check(type == GLES20.GL_FLOAT_VEC2)

                GLES20.glUniform2f(location, first, second)
            }
        }

        class GlFloatMat4(
            private val floatArray: FloatArray
        ): Value() {
            override fun bind(location: Int, type: Int) {
                check(type == GLES20.GL_FLOAT_MAT4) { "wrong type" }

                GLES20.glUniformMatrix4fv(location, 1, false, floatArray, 0)
            }
        }

        class GlSampler(
            private val texture: Int,
            private val unit: Int
        ): Value() {
            override fun bind(location: Int, type: Int) {
                check(type == GLES11Ext.GL_SAMPLER_EXTERNAL_OES || type == GLES20.GL_SAMPLER_2D) { "wrong type" }

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit)
                when (type) {
                    GLES11Ext.GL_SAMPLER_EXTERNAL_OES -> GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture)
                    GLES20.GL_SAMPLER_2D -> GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
                }
                GLES20.glUniform1i(location, unit)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
            }
        }

    }
}