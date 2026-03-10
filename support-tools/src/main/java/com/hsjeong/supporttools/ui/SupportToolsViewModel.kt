package com.hsjeong.supporttools.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.hsjeong.supporttools.utils.UrlConfigUtil

class SupportToolsViewModel() : ViewModel() {
    private val _state = mutableStateOf(SupportToolsState())
    val state = _state

    fun processIntent(intent: SupportToolsIntent) {
        when (intent) {
            is SupportToolsIntent.Init -> {
                _state.value = _state.value.copy(
                    selectedServer = intent.serverType,
                    selectUrls = UrlConfigUtil.getUrls(intent.serverType),
                    showScreenName = intent.showScreenName,
                    showNetworkLog = intent.showNetworkLog,
                    enableServerChange = intent.enableServerChange
                )
            }
            is SupportToolsIntent.SelectServer -> {
                _state.value = _state.value.copy(
                    selectedServer = intent.serverType,
                    selectUrls = UrlConfigUtil.getUrls(intent.serverType)
                )
            }
            is SupportToolsIntent.ToggleScreenName -> {
                _state.value = _state.value.copy(
                    showScreenName = intent.checked
                )
            }
            is SupportToolsIntent.ToggleNetworkLog -> {
                _state.value = _state.value.copy(
                    showNetworkLog = intent.checked
                )
            }
            is SupportToolsIntent.ToggleServerChange -> {
                _state.value = _state.value.copy(
                    enableServerChange = intent.checked
                )
            }

        }
    }
}