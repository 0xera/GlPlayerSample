package com.uzicus.glplayersample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uzicus.glplayersample.databinding.ActivityMainBinding
import com.uzicus.glplayersample.file.FilePickerImpl
import com.uzicus.glplayersample.player.ExoPlayerController
import com.uzicus.glplayersample.player.PlayerController
import com.uzicus.glplayersample.processing.effects.ChromaKeyShader
import com.uzicus.glplayersample.processing.effects.TranslucentOverlayShader
import com.uzicus.glplayersample.processing.effects.TranslucentShader
import com.uzicus.glplayersample.utils.doOnApplyWindowInsets
import com.uzicus.glplayersample.utils.loadAsBitmap
import com.uzicus.glplayersample.utils.statusBarHeight

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return MainViewModel(filePickerImpl) as T
            }
        }
    }

    private val filePickerImpl by lazy { FilePickerImpl(this) }
    private val playerController: PlayerController by lazy { ExoPlayerController(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        viewBinding.lifecycleOwner = this
        viewBinding.viewModel = viewModel

        WindowCompat.setDecorFitsSystemWindows(window, false)
        viewBinding.contentLayout.doOnApplyWindowInsets { initialPadding ->
            viewBinding.contentLayout.updatePadding(
                top = initialPadding.top + statusBarHeight
            )
        }

        playerController.attachGlSurfaceView(viewBinding.glSurfaceView)

        viewModel.selectedEffect.observe(this) { type ->
            val shader = when (type) {
                EffectType.CHROMA_KEY -> ChromaKeyShader(applicationContext)
                EffectType.TRANSLUCENT -> TranslucentShader(applicationContext)
                EffectType.OVERLAY -> TranslucentOverlayShader(applicationContext, assets.loadAsBitmap("king.png"))
                else -> null
            }
            playerController.applyShader(shader)
        }

        viewModel.play.observe(this) { url ->
            playerController.play(url)
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
}