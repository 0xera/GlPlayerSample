package com.uzicus.glplayersample

import androidx.lifecycle.*
import com.uzicus.glplayersample.file.FileInfo
import com.uzicus.glplayersample.file.FilePicker
import com.uzicus.glplayersample.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class MainViewModel(
    private val filePicker: FilePicker
) : ViewModel() {

    private val _selectedEffect = MutableLiveData<EffectType>(null)
    private val _content = MutableLiveData<FileInfo?>(null)

    private val _play = SingleLiveEvent<String>()
    private val _showMsg = SingleLiveEvent<String>()
    private val _pauseResume = SingleLiveEvent<Unit>()

    val effects: LiveData<List<EffectType>> = MutableLiveData(EffectType.values().toList())
    val selectedEffect: LiveData<EffectType> = _selectedEffect
    val fileName: LiveData<String> = Transformations.map(_content) { it?.fileName ?: "choose video file" }
    val isPlayButtonEnabled = Transformations.map(_content) { it?.uri.isNullOrEmpty().not() }
    val play: LiveData<String> = _play
    val pauseResume: LiveData<Unit> = _pauseResume

    val showMsg: LiveData<String> = _showMsg

    fun onChooseClicks() {
        viewModelScope.launch {
            val content = filePicker.chooseFile()
            _content.postValue(content)
        }
    }

    fun onPlayClicks() {
        val url = _content.value?.uri ?: return
        _play.postValue(url)
    }

    fun onPauseResumeClicks() {
        _pauseResume.postValue(Unit)
    }

    fun onGiftTypeSelected(typePosition: Int) {
        val type = EffectType.values()[typePosition]
        _selectedEffect.postValue(type)
    }

}