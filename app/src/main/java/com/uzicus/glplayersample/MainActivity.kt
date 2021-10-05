package com.uzicus.glplayersample

import android.os.Bundle
import android.view.Surface
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.exoplayer2.ExoPlayer
import com.uzicus.glplayersample.databinding.ActivityMainBinding
import com.uzicus.glplayersample.file.FilePickerImpl
import com.uzicus.glplayersample.processing.VideoProcessingGLSurfaceView
import com.uzicus.glplayersample.processing.effects.TranslucentVideoEffect
import com.uzicus.glplayersample.processing.effects.VideoEffect

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return MainViewModel(filePickerImpl) as T
            }
        }
    }

    private val filePickerImpl by lazy { FilePickerImpl(this) }
    private val playerController by lazy { PlayerController(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        viewBinding.lifecycleOwner = this
        viewBinding.viewModel = viewModel

        playerController.setVideoSizeChangeListener(object : PlayerController.VideoSizeChangeListener {
            override fun onVideoSizeChanged(height: Int, width: Int, pixelWidthHeightRatio: Float) {
                val measuredVideoAspect = width.toFloat() / height * pixelWidthHeightRatio
                viewBinding.glSurfaceView.onVideoAspectChanged(measuredVideoAspect)
            }
        })

        viewModel.selectedEffect.observe(this) { effect ->
            viewBinding.glSurfaceView.applyVideoEffect(effect)
        }

        viewModel.play.observe(this) { url ->
            playerController.play(url)
            val videoComponent = playerController.videoComponent ?: return@observe
            viewBinding.glSurfaceView.setSurfaceListener(ExoSurfaceHolder(videoComponent))
        }

        viewModel.pauseResume.observe(this) {
            playerController.pauseOrResume()
        }

        viewModel.showMsg.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        filePickerImpl.detach()
    }

    private fun VideoProcessingGLSurfaceView.applyVideoEffect(effectType: EffectType?) {
        val videoEffect: VideoEffect = when (effectType) {
            EffectType.TRANSLUCENT -> TranslucentVideoEffect(applicationContext)
            else -> null
        } ?: return
        setVideoEffect(videoEffect)
    }

    private class ExoSurfaceHolder(
        private val videoComponent: ExoPlayer.VideoComponent
    ): VideoProcessingGLSurfaceView.SurfaceListener {
        override fun clearVideoSurface(surface: Surface?) {
            videoComponent.clearVideoSurface(surface)
        }

        override fun setVideoSurface(surface: Surface?) {
            videoComponent.setVideoSurface(surface)
        }
    }

}