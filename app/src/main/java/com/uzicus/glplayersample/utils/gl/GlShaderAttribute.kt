package com.uzicus.glplayersample.utils.gl

import android.opengl.GLES20
import com.uzicus.glplayersample.utils.gl.EglUtils.strLen
import com.uzicus.glplayersample.utils.gl.EglUtils.toBuffer
import java.nio.Buffer

class GlShaderAttribute(
    program: Int,
    private val index: Int
) {

    companion object {

        fun getAllFromProgram(program: Int): Array<GlShaderAttribute> {
            val attributeCount = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTES, attributeCount, 0)

            return Array(attributeCount[0]) { index ->
                GlShaderAttribute(program, index)
            }
        }
    }

    val name: String
    val location: Int

    private var buffer: Buffer? = null
    private var size = 0

    init {
        val len = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, len, 0)
        val type = IntArray(1)
        val size = IntArray(1)
        val nameBytes = ByteArray(len[0])
        val ignore = IntArray(1)
        GLES20.glGetActiveAttrib(program, index, len[0], ignore, 0, size, 0, type, 0, nameBytes, 0)
        name = String(nameBytes, 0, nameBytes.strLen)
        location = GLES20.glGetAttribLocation(program, name)
    }

    fun setBuffer(buffer: FloatArray, size: Int) {
        this.buffer = buffer.toBuffer()
        this.size = size
    }

    fun bind() {
        val buffer = buffer ?: error("call setBuffer before bind")

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glVertexAttribPointer(
            location,
            size,  // count
            GLES20.GL_FLOAT,  // type
            false,  // normalize
            0,  // stride
            buffer
        )
        GLES20.glEnableVertexAttribArray(index)
        EglUtils.checkGlError()
    }

}