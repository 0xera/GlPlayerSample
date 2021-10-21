package com.uzicus.glplayersample.processing

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.os.Handler
import android.os.Looper
import android.view.Surface
import com.uzicus.glplayersample.utils.gl.EglUtils
import java.util.concurrent.atomic.AtomicBoolean

class FrameTextureHolder(
    private val stMatrix: FloatArray,
    private val onFrameAvailable: () -> Unit
) {

    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    private var surfaceTextureId = -1
    private var surfaceTexture: SurfaceTexture? = null
    private var surfaceHolder: SurfaceHolder? = null

    private val frameAvailable = AtomicBoolean()

    fun setSurfaceHolder(newSurfaceHolder: SurfaceHolder) {
        if (surfaceHolder === newSurfaceHolder) return

        val previousSurface = surfaceHolder?.getSurface()

        if (previousSurface != null) {
            surfaceHolder?.clearSurface(previousSurface)
            newSurfaceHolder.setSurface(previousSurface)
        } else {
            val newSurface = Surface(surfaceTexture)
            newSurfaceHolder.setSurface(newSurface)
        }

        surfaceHolder = newSurfaceHolder
    }

    // call only from Gl Thread
    fun onGlSurfaceCreated() {
        surfaceTextureId = EglUtils.createTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
        surfaceTexture = SurfaceTexture(surfaceTextureId)
        surfaceTexture?.setOnFrameAvailableListener {
            frameAvailable.set(true)
            onFrameAvailable()
        }

        mainHandler.post {
            release()
            val newSurface = Surface(surfaceTexture)
            surfaceHolder?.setSurface(newSurface)
        }
    }

    fun release() {
        surfaceTexture?.release()
        surfaceHolder?.release()
    }

    fun queryFrameTexture(): Int {
        if (frameAvailable.compareAndSet(true, false)) {
            surfaceTexture?.updateTexImage()
            surfaceTexture?.getTransformMatrix(stMatrix)
        }

        return surfaceTextureId
    }

}