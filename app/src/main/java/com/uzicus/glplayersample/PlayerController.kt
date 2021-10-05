package com.uzicus.glplayersample

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoSize

class PlayerController(private val context: Context) {

    interface VideoSizeChangeListener {
        fun onVideoSizeChanged(height: Int, width: Int, pixelWidthHeightRatio: Float)
    }

    private var player: SimpleExoPlayer? = null
    private var videoSizeChangeListener: VideoSizeChangeListener? = null

    private val dataSourceFactory = DefaultDataSourceFactory(context)
    private val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)

    private val isPlaying: Boolean
        get() = player?.playWhenReady == true && player?.playbackState != Player.STATE_ENDED && player?.playbackState != Player.STATE_IDLE

    val videoComponent: ExoPlayer.VideoComponent?
        get() = player?.videoComponent

    fun setVideoSizeChangeListener(videoSizeChangeListener: VideoSizeChangeListener) {
        this.videoSizeChangeListener = videoSizeChangeListener
    }

    fun pauseOrResume() {
        player?.playWhenReady = isPlaying.not()
    }

    fun play(url: String) {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return
        val mediaItem = MediaItem.Builder().setUri(uri).build()
        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)

        val player = player ?: createExoPlayer().also { player = it }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(Util.getAudioUsageForStreamType(C.STREAM_TYPE_MUSIC))
            .setContentType(Util.getAudioContentTypeForStreamType(C.STREAM_TYPE_MUSIC))
            .build()

        player.setAudioAttributes(audioAttributes, false)
        player.volume = 1F

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
                videoSizeChangeListener?.onVideoSizeChanged(
                    videoSize.height,
                    videoSize.width,
                    videoSize.pixelWidthHeightRatio
                )
            }
        })

        player.playWhenReady = true
        player.setMediaSource(mediaSource)
        player.prepare()
    }

    private fun createExoPlayer(): SimpleExoPlayer {
        val trackSelector = DefaultTrackSelector(context)

        return SimpleExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build().apply {
                addAnalyticsListener(EventLogger(trackSelector))
            }
    }
}