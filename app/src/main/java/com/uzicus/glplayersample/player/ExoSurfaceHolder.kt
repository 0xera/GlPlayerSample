package com.uzicus.glplayersample.player

import android.view.Surface
import com.google.android.exoplayer2.ExoPlayer
import com.uzicus.glplayersample.processing.SurfaceHolder

class ExoSurfaceHolder(
    private val videoComponent: ExoPlayer.VideoComponent
): SurfaceHolder {

    private var surface: Surface? = null

    override fun clearSurface(surface: Surface?) {
        videoComponent.clearVideoSurface(surface)
    }

    override fun setSurface(surface: Surface?) {
        this.surface = surface
        videoComponent.setVideoSurface(surface)
    }

    override fun getSurface(): Surface? {
        return surface
    }

    override fun release() {
        surface?.release()
        surface = null
    }

}