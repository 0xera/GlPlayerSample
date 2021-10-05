package com.uzicus.glplayersample.processing

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Surface
import com.uzicus.glplayersample.processing.setup.AppEGLConfigChooser
import com.uzicus.glplayersample.processing.setup.AppEGLContextFactory
import com.uzicus.glplayersample.processing.effects.VideoEffect

class VideoProcessingGLSurfaceView(
    context: Context,
    attrs: AttributeSet?
) : GLSurfaceView(context, attrs) {

    interface SurfaceListener {
        fun clearVideoSurface(surface: Surface?)
        fun setVideoSurface(surface: Surface?)
    }

    enum class ScaleType { FIT_WIDTH, FIT_HEIGHT, NONE }

    private val renderer: VideoRenderer = VideoRenderer(this::onSurfaceTextureAvailable)

    private var effectAspectFactor = 1F
    private var measuredVideoAspect = 1F

    private var playerScaleType = ScaleType.FIT_WIDTH

    private val mainHandler: Handler = Handler(Looper.getMainLooper())
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private var surfaceListener: SurfaceListener? = null

    init {
        setEGLContextFactory(AppEGLContextFactory())
        setEGLConfigChooser(
            AppEGLConfigChooser(
                redSize = 8,
                greenSize = 8,
                blueSize = 8,
                alphaSize = 8
            )
        )
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setSurfaceListener(newSurfaceListener: SurfaceListener) {
        if (surfaceListener === newSurfaceListener) {
            return
        }
        if (surface != null) {
            surfaceListener?.clearVideoSurface(surface)
        }
        surfaceListener = newSurfaceListener
        surfaceListener?.setVideoSurface(surface)
    }

    fun setVideoEffect(videoEffect: VideoEffect) {
        renderer.setVideoEffect(videoEffect)
        holder.setFormat(videoEffect.pixelFormat)
        setZOrderOnTop(PixelFormat.formatHasAlpha(videoEffect.pixelFormat))
        effectAspectFactor = videoEffect.aspectFactor

        requestRender()
        requestLayout()
    }

    fun onVideoAspectChanged(videoAspect: Float) {
        measuredVideoAspect = videoAspect
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        var viewWidth = measuredWidth
        var viewHeight = measuredHeight

        when (playerScaleType) {
            ScaleType.FIT_WIDTH -> viewHeight = (measuredWidth / (measuredVideoAspect * effectAspectFactor)).toInt()
            ScaleType.FIT_HEIGHT -> viewWidth = (measuredHeight * (measuredVideoAspect * effectAspectFactor)).toInt()
            ScaleType.NONE -> { /* nothing */ }
        }

        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Post to make sure we occur in order with any onSurfaceTextureAvailable calls.
        mainHandler.post {
            if (surface != null) {
                surfaceListener?.setVideoSurface(null)
                releaseSurface(surfaceTexture, surface)
                surfaceTexture = null
                surface = null
            }
        }
    }

    private fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture) {
        surfaceTexture.setOnFrameAvailableListener {
            renderer.onFrameAvailable()
            requestRender()
        }
        mainHandler.post {
            val oldSurfaceTexture = this.surfaceTexture
            val oldSurface = surface
            this.surfaceTexture = surfaceTexture
            surface = Surface(surfaceTexture)
            releaseSurface(oldSurfaceTexture, oldSurface)
            surfaceListener?.setVideoSurface(surface)
        }
    }

    private fun releaseSurface(oldSurfaceTexture: SurfaceTexture?, oldSurface: Surface?) {
        oldSurfaceTexture?.release()
        oldSurface?.release()
    }
}