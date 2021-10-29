package com.uzicus.glplayersample.processing

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Size
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.doOnDetach
import com.uzicus.glplayersample.GLTextureView
import com.uzicus.glplayersample.processing.effects.shader.PreviewShader
import com.uzicus.glplayersample.processing.effects.shader.Shader
import com.uzicus.glplayersample.utils.gl.GlFrameBufferWrapper
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class VideoRenderer(
    private val glTextureView: GLTextureView
) : GLTextureView.Renderer {

    enum class ScaleType { FIT_WIDTH, FIT_HEIGHT, NONE }

    private val frameStMatrix = FloatArray(16)

    private val frameTextureHolder = FrameTextureHolder(frameStMatrix, onFrameAvailable = {
        glTextureView.requestRender()
    })

    private val frameBuffer = GlFrameBufferWrapper()
    private val framePreviewShader = PreviewShader(frameStMatrix)

    private var activeShader: Shader? = null

    private var scaleType = ScaleType.FIT_WIDTH
    private var surfaceWidth = -1
    private var surfaceHeight = -1
    private var frameWidth = -1
    private var frameHeight = -1
    private var videoAspect: Float = 1F

    private val isSizeChanged = AtomicBoolean(false)
    private val isPreviewInitialized = AtomicBoolean()
    private val isEffectInitialized = AtomicBoolean(false)

    init {
        glTextureView.isOpaque = false
        glTextureView.apply {
            setEGLContextClientVersion(2)
            setEGLConfigChooser(
                redSize = 8,
                greenSize = 8,
                blueSize = 8,
                alphaSize = 8
            )
            setRenderer(this@VideoRenderer)
            setRenderMode(GLTextureView.RENDERMODE_WHEN_DIRTY)
            doOnDetach { frameTextureHolder.release() }
        }
    }

    fun setShader(newShader: Shader?) {
        activeShader = newShader

        isEffectInitialized.set(false)
        isSizeChanged.set(true)

        glTextureView.requestRender()
    }

    fun onVideoAspectChanged(videoAspect: Float) {
        this.videoAspect = videoAspect
        isSizeChanged.set(true)
    }

    fun setSurfaceHolder(newSurfaceHolder: SurfaceHolder) {
        frameTextureHolder.setSurfaceHolder(newSurfaceHolder)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        frameTextureHolder.onGlSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        isSizeChanged.set(true)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClearColor(0F, 0F, 0F, 0F)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        val frameTexture = frameTextureHolder.queryFrameTexture()

        if (isEffectInitialized.compareAndSet(false, true)) {
            activeShader?.initialize()
        }

        if (isPreviewInitialized.compareAndSet(false, true)) {
            framePreviewShader.initialize()
        }

        if (isSizeChanged.compareAndSet(true, false)) {
            val (width, height) = measureSize()
            val yOffset = (surfaceHeight - height) / 2
            val xOffset = (surfaceWidth - width) / 2

            GLES20.glViewport(xOffset, yOffset, width, height)

            frameBuffer.setup(width, height)
            frameWidth = width
            frameHeight = height
        }

        if (frameWidth == -1 || frameHeight == -1) return

        if (activeShader != null) {
            frameBuffer.recordFrame {
                framePreviewShader.setInputTexture(frameTexture, frameWidth, frameHeight)
                framePreviewShader.draw()
            }
            activeShader?.setInputTexture(frameBuffer.texName, frameWidth, frameHeight)
            activeShader?.draw()
        } else {
            frameBuffer.release()
            Matrix.setIdentityM(frameStMatrix, 0)

            framePreviewShader.setInputTexture(frameTexture, frameWidth, frameHeight)
            framePreviewShader.draw()
        }
    }

    private fun measureSize(): Size {
        val aspectRatio = videoAspect * (activeShader?.adjustAspect ?: 1F)

        var height = surfaceHeight
        var width = surfaceWidth

        when (scaleType) {
            ScaleType.FIT_WIDTH -> height = (surfaceWidth / aspectRatio).toInt()
            ScaleType.FIT_HEIGHT -> width = (surfaceHeight * aspectRatio).toInt()
            ScaleType.NONE -> { /* nothing */ }
        }

        return Size(width, height)
    }

}