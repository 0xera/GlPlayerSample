package com.uzicus.glplayersample.player

import android.content.Context
import android.net.Uri
import android.opengl.GLSurfaceView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.video.VideoSize
import com.uzicus.glplayersample.processing.SurfaceHolder
import com.uzicus.glplayersample.processing.VideoRenderer
import com.uzicus.glplayersample.processing.effects.shader.Shader

class ExoPlayerController(private val context: Context): PlayerController {

    private var renderer: VideoRenderer? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var player: SimpleExoPlayer? = null

    private val dataSourceFactory = DefaultDataSourceFactory(context)
    private val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)

    private val isPlaying: Boolean
        get() = player?.playWhenReady == true && player?.playbackState != Player.STATE_ENDED && player?.playbackState != Player.STATE_IDLE

    private fun createExoPlayer(): SimpleExoPlayer {
        val trackSelector = DefaultTrackSelector(context)

        val player = SimpleExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build().apply {
                addAnalyticsListener(EventLogger(trackSelector))
            }

        return player
    }

    override fun attachGlSurfaceView(glSurfaceView: GLSurfaceView) {
        renderer = VideoRenderer(glSurfaceView)
    }

    override fun applyShader(shader: Shader?) {
        renderer?.setShader(shader)
    }

    override fun pauseOrResume() {
        player?.playWhenReady = isPlaying.not()
    }

    override fun play(url: String) {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: error("wrong url: $url")
        val mediaItem = MediaItem.Builder().setUri(uri).build()
        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)

        val player = player ?: createExoPlayer().also { player = it }
        val surfaceHolder = surfaceHolder ?: ExoSurfaceHolder(player).also { surfaceHolder = it }

        renderer?.setSurfaceHolder(surfaceHolder)

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        player.playWhenReady = true
                        player.play()
                    }
                    Player.STATE_IDLE,
                    Player.STATE_ENDED -> {
                        player.removeListener(this)
                    }
                    else -> { /* nothing */}
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                player.removeListener(this)
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                val videoAspect = videoSize.width.toFloat() / videoSize.height * videoSize.pixelWidthHeightRatio
                renderer?.onVideoAspectChanged(videoAspect)
            }
        })

        player.playWhenReady = true
        player.setMediaSource(mediaSource)
        player.prepare()
    }
}