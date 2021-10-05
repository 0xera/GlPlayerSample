package com.uzicus.glplayersample.processing

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.uzicus.glplayersample.processing.effects.VideoEffect
import com.uzicus.glplayersample.utils.EglUtils
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class VideoRenderer(
    private val onSurfaceTextureAvailable: (SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "VideoRenderer"
    }

    private var videoEffect: VideoEffect? = null

    private val frameAvailable: AtomicBoolean = AtomicBoolean()
    private var texture = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var isInitialized = false
    private var width: Int = -1
    private var height: Int = -1

    fun setVideoEffect(newVideoEffect: VideoEffect?) {
        videoEffect = newVideoEffect
        isInitialized = false
    }

    fun onFrameAvailable() {
        frameAvailable.set(true)
    }

    @Synchronized
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.d(TAG, "onSurfaceCreated")

        texture = EglUtils.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
        surfaceTexture = SurfaceTexture(texture)
        onSurfaceTextureAvailable(surfaceTexture!!)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: width: $width, height: $height")

        GLES20.glViewport(0, 0, width, height)
        this.width = width
        this.height = height
    }

    override fun onDrawFrame(gl: GL10) {
        Log.d(TAG, "onDrawFrame")

        val videoProcessor = videoEffect ?: return

        if (!isInitialized) {
            videoProcessor.initialize()
            isInitialized = true
        }
        if (width != -1 && height != -1) {
            videoProcessor.setSurfaceSize(width, height)
            width = -1
            height = -1
        }
        if (frameAvailable.compareAndSet(true, false)) {
            surfaceTexture?.updateTexImage()
        }
        videoProcessor.draw(texture, surfaceTexture?.timestamp ?: 0)
    }

}