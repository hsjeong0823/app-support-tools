package com.hsjeong.supporttools.ui

import com.hsjeong.supporttools.utils.UrlConfigUtil

// Activity로 전달되는 이벤트 정의
sealed class SupportToolsUiEvent {
    data object Close : SupportToolsUiEvent()
    data object Apply : SupportToolsUiEvent()
    data object MovePreferenceViewer : SupportToolsUiEvent()
}

// ViewModel로 전달되는 Intent 정의
sealed class SupportToolsIntent {
    data class Init(
        val serverType: UrlConfigUtil.ServerType,
        val showScreenName: Boolean,
        val showLogcatViewer: Boolean,
        val showNetworkLog: Boolean,
        val enableServerChange: Boolean
    ) : SupportToolsIntent()

    data class SelectServer(val serverType: UrlConfigUtil.ServerType) : SupportToolsIntent()
    data class ToggleScreenName(val checked: Boolean) : SupportToolsIntent()
    data class ToggleLogcatViewer(val enable: Boolean) : SupportToolsIntent()
    data class ToggleNetworkLog(val checked: Boolean) : SupportToolsIntent()
    data class ToggleServerChange(val checked: Boolean) : SupportToolsIntent()
}

// Compose에서 사용되는 State 정의
data class SupportToolsState(
    val selectedServer: UrlConfigUtil.ServerType = UrlConfigUtil.ServerType.DEV,
    val selectUrls: List<String> = emptyList(),
    val showScreenName: Boolean = false,
    val showLogcatViewer: Boolean = false,
    val showNetworkLog: Boolean = false,
    val enableServerChange: Boolean = false
)