package com.uzicus.glplayersample.utils.gl

import android.opengl.GLES20
import android.opengl.GLU
import android.util.Log
import java.lang.RuntimeException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

object EglUtils {

    private const val TAG = "EglUtils"

    private const val BYTES_PER_FLOAT = 4

    fun compileProgram(vertexCode: String?, fragmentCode: String?): Int {
        val program = GLES20.glCreateProgram()
        checkGlError()

        // Add the vertex and fragment shaders.
        addShader(GLES20.GL_VERTEX_SHADER, vertexCode!!, program)
        addShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode!!, program)

        // Link and check for errors.
        GLES20.glLinkProgram(program)
        val linkStatus = intArrayOf(GLES20.GL_FALSE)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            val errorMsg = """
                Unable to link shader program: 
                ${GLES20.glGetProgramInfoLog(program)}
            """.trimIndent()
            Log.e(TAG, errorMsg)
            throw RuntimeException(errorMsg)
        }
        checkGlError()
        return program
    }

    private fun addShader(type: Int, source: String, program: Int) {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val result = intArrayOf(GLES20.GL_FALSE)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, result, 0)
        if (result[0] != GLES20.GL_TRUE) {
            val errorMsg = "${GLES20.glGetShaderInfoLog(shader)}, source: $source"
            Log.e(TAG, errorMsg)
            throw RuntimeException(errorMsg)
        }
        GLES20.glAttachShader(program, shader)
        GLES20.glDeleteShader(shader)
        checkGlError()
    }

    fun createTexture(target: Int, mag: Int = GLES20.GL_LINEAR, min: Int = GLES20.GL_LINEAR): Int {
        // new texture
        val texIdResult = IntArray(1)
        GLES20.glGenTextures(1, IntBuffer.wrap(texIdResult))
        val texId = texIdResult[0]

        // enable
        GLES20.glBindTexture(target, texId)

        // sampler
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MAG_FILTER, mag)
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MIN_FILTER, min)
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        checkGlError()
        return texId
    }

    fun checkGlError() {
        var lastError = GLES20.GL_NO_ERROR
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "glError " + GLU.gluErrorString(error))
            lastError = error
        }
        if (lastError != GLES20.GL_NO_ERROR) {
            throw RuntimeException("glError " + GLU.gluErrorString(lastError))
        }
    }

    fun FloatArray.toBuffer(): Buffer {
        val byteBuffer = ByteBuffer.allocateDirect(size * BYTES_PER_FLOAT)
        val floatBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer()

        return floatBuffer.put(this).flip()
    }

    val ByteArray.strLen: Int
        get() = indexOfFirst { it == '\u0000'.code.toByte() }.takeIf { it != -1 } ?: size

}

