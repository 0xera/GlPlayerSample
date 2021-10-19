package com.uzicus.glplayersample.processing.effects.shader

import android.opengl.Matrix
import com.uzicus.glplayersample.utils.gl.GlShaderUniform
import com.uzicus.glplayersample.utils.gl.GlShaderUniform.Companion.bind

class PreviewShader(
    private val stMatrix: FloatArray
): BaseShader() {

    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    override val vertexShaderCode: String = """
        attribute vec4 $ATTRIBUTE_POSITION_NAME;
        attribute vec4 $ATTRIBUTE_TEXTURE_COORDINATE_NAME;
                
        uniform mat4 uMVPMatrix;
        uniform mat4 uSTMatrix;
        uniform vec2 $UNIFORM_RESOLUTION;
        
        varying highp vec2 vTextureCoord;
        
        void main() {
            vec4 scaledPos = $ATTRIBUTE_POSITION_NAME;
            scaledPos.x *= $UNIFORM_RESOLUTION.x / $UNIFORM_RESOLUTION.y;
            gl_Position = uMVPMatrix * scaledPos;
            vTextureCoord = (uSTMatrix * $ATTRIBUTE_TEXTURE_COORDINATE_NAME).xy;
        }
    """.trimIndent()

    override val fragmentShaderCode: String = """
        #extension GL_OES_EGL_image_external : require
        
        precision mediump float;
        
        uniform lowp samplerExternalOES $UNIFORM_FRAME_TEXTURE_SAMPLER_NAME;
        varying highp vec2 vTextureCoord;
        
        void main() {
            gl_FragColor = texture2D($UNIFORM_FRAME_TEXTURE_SAMPLER_NAME, vTextureCoord);
        }
    """.trimIndent()

    override fun setInputTexture(texture: Int, width: Int, height: Int) {
        super.setInputTexture(texture, width, height)

        // camera
        Matrix.setLookAtM(
            viewMatrix, 0,
            0.0f, 0.0f, 5.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f
        )

        val aspectRatio = width.toFloat() / height
        Matrix.frustumM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, 5f, 7f)
        Matrix.setIdentityM(mMatrix, 0)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, mMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
    }

    override fun defineUniform(uniform: GlShaderUniform) {
        when (uniform.name) {
            "uSTMatrix" -> uniform bind GlShaderUniform.Value.GlFloatMat4(stMatrix)
            "uMVPMatrix" -> uniform bind GlShaderUniform.Value.GlFloatMat4(mvpMatrix)
            else -> super.defineUniform(uniform)
        }
    }

}