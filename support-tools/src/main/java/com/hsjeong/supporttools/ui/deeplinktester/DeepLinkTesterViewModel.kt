package com.hsjeong.supporttools.ui.deeplinktester

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.hsjeong.supporttools.utils.DeepLinkManager

class DeepLinkTesterViewModel : ViewModel() {
    private val _state = mutableStateOf(DeepLinkTesterState())
    val state = _state

    init {
        // 매니저로부터 저장된 리스트를 불러옴
        _state.value = _state.value.copy(
            predefinedDeepLinks = DeepLinkManager.getDeepLinkList()
        )
    }

    fun updateUri(uri: String) {
        _state.value = _state.value.copy(uriText = uri)
    }
}