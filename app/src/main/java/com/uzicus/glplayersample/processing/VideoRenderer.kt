package com.uzicus.glplayersample.processing

import android.graphics.PixelFormat
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Size
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.doOnDetach
import com.uzicus.glplayersample.processing.effects.shader.PreviewShader
import com.uzicus.glplayersample.processing.effects.shader.Shader
import com.uzicus.glplayersample.processing.setup.AppEGLConfigChooser
import com.uzicus.glplayersample.processing.setup.AppEGLContextFactory
import com.uzicus.glplayersample.utils.gl.GlFrameBufferWrapper
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class VideoRenderer(
    private val glSurfaceView: GLSurfaceView
) : GLSurfaceView.Renderer {

    enum class ScaleType { FIT_WIDTH, FIT_HEIGHT, NONE }

    private val frameStMatrix = FloatArray(16)

    private val surfaceTextureHolder = SurfaceTextureHolder(frameStMatrix, glSurfaceView)
    private val frameBuffer = GlFrameBufferWrapper()
    private val framePreviewShader = PreviewShader(frameStMatrix)

    private var activeShader: Shader? = null

    private var playerScaleType = ScaleType.FIT_WIDTH
    private var surfaceWidth = -1
    private var surfaceHeight = -1
    private var videoAspect: Float = 1F

    private val isSizeChanged = AtomicBoolean(false)
    private val isPreviewInitialized = AtomicBoolean()
    private val isEffectInitialized = AtomicBoolean(false)

    init {
        glSurfaceView.holder.setFormat(PixelFormat.RGBA_8888)
        glSurfaceView.apply {
            setZOrderOnTop(true)
            setEGLContextFactory(AppEGLContextFactory())
            setEGLConfigChooser(
                AppEGLConfigChooser(
                    redSize = 8,
                    greenSize = 8,
                    blueSize = 8,
                    alphaSize = 8
                )
            )
            setRenderer(this@VideoRenderer)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            doOnDetach { surfaceTextureHolder.release() }
        }
    }

    fun setShader(newShader: Shader?) {
        activeShader = newShader

        isEffectInitialized.set(false)
        isSizeChanged.set(true)

        glSurfaceView.requestRender()
    }

    fun onVideoAspectChanged(videoAspect: Float) {
        this.videoAspect = videoAspect
        isSizeChanged.set(true)
    }

    fun setSurfaceHolder(newSurfaceHolder: SurfaceHolder) {
        surfaceTextureHolder.setSurfaceHolder(newSurfaceHolder)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        surfaceTextureHolder.onGlSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        isSizeChanged.set(true)
    }

    override fun onDrawFrame(gl: GL10) {
        val frameTexture = surfaceTextureHolder.queryFrameTexture()

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

            framePreviewShader.setInputTexture(frameTexture, width, height)
            frameBuffer.setup(width, height)
        }

        if (activeShader != null) {
            frameBuffer.recordFrame { framePreviewShader.draw() }

            activeShader?.setInputTexture(frameBuffer.texName, frameBuffer.width, frameBuffer.height)
            activeShader?.draw()
        } else {
            framePreviewShader.setInputTexture(frameBuffer.texName, frameBuffer.width, frameBuffer.height)
            framePreviewShader.draw()
        }
    }

    private fun measureSize(): Size {
        val aspectRatio = videoAspect * (activeShader?.adjustAspect ?: 1F)

        var height = surfaceHeight
        var width = surfaceWidth

        when (playerScaleType) {
            ScaleType.FIT_WIDTH -> height = (surfaceWidth / aspectRatio).toInt()
            ScaleType.FIT_HEIGHT -> width = (surfaceHeight * aspectRatio).toInt()
            ScaleType.NONE -> { /* nothing */ }
        }

        return Size(width, height)
    }

}