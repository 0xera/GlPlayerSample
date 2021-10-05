package com.uzicus.glplayersample.utils

import android.opengl.GLES20
import android.opengl.GLU
import android.util.Log
import com.google.android.exoplayer2.util.GlUtil
import java.lang.RuntimeException
import java.nio.IntBuffer

object EglUtils {

    private const val TAG = "EglUtils"

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

    fun getAttributes(program: Int): Array<GlUtil.Attribute> {
        val attributeCount = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_ACTIVE_ATTRIBUTES, attributeCount, 0)

        return Array(attributeCount[0]) { index ->
            GlUtil.Attribute(program, index)
        }
    }

    private fun checkGlError() {
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

}

