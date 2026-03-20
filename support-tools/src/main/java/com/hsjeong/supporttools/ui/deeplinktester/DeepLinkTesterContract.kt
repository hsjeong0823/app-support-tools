package com.hsjeong.supporttools.ui.deeplinktester

import com.hsjeong.supporttools.utils.DeepLinkData

sealed class DeepLinkTesterUiEvent {
    data object Close : DeepLinkTesterUiEvent()
    data class DeeplinkInputText(val inputText: String) : DeepLinkTesterUiEvent()
    data class DeeplinkLaunch(val uri: String) : DeepLinkTesterUiEvent()
    data class CopyToClipboard(val uri: String) : DeepLinkTesterUiEvent()
}

data class DeepLinkTesterState(
    val uriText: String = "",
    val predefinedDeepLinks: List<DeepLinkData> = emptyList()
)