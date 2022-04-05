package com.example.overplay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _isRightGesture = MutableLiveData<Boolean>()
    val isRightGesture: LiveData<Boolean> = _isRightGesture

    private val _isUpwardGesture = MutableLiveData<Boolean>()
    val isUpwardGesture: LiveData<Boolean> = _isUpwardGesture

    private var searchJob: Job? = null

    fun isRightGestureDebounced(isRightGesture: Boolean) {
        debounce {
            _isRightGesture.postValue(isRightGesture)
        }
    }

    fun isUpwardGestureDebounced(isUpwardGesture: Boolean) {
        debounce {
            _isUpwardGesture.postValue(isUpwardGesture)
        }
    }

    private inline fun debounce(crossinline block: () -> Unit) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            runCatching {
                delay(DEBOUNCE_DELAY)
            }.onSuccess {
                block.invoke()
            }.onFailure {
                searchJob?.cancel()
            }
        }
    }

    companion object {
        private const val DEBOUNCE_DELAY = 1000L
    }

}
