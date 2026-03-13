package com.hsjeong.supporttools.ui.logviewer

import com.hsjeong.supporttools.utils.LogcatOverlayUtil

// Activity로 전달되는 이벤트 정의
sealed class LogViewerUiEvent {
    data object Close : LogViewerUiEvent()
    data object Clear : LogViewerUiEvent()
    data class SearchLogData(val searchText: String) : LogViewerUiEvent()
    data object ShareLog : LogViewerUiEvent()
}

// ViewModel로 전달되는 Intent 정의
sealed class LogViewerIntent {
    data class LoadLogData(val searchText: String = "") : LogViewerIntent()
    data object ClearLogData : LogViewerIntent()
    data class SearchLogData(val searchText: String) : LogViewerIntent()
}

// Compose에서 사용되는 State 정의
data class LogViewerState(
    val logData: List<LogcatOverlayUtil.LogItemData> = emptyList(),
    val searchedLogData: List<LogcatOverlayUtil.LogItemData> = emptyList(), // 화면에 보일 필터링된 데이터
    val searchText: String = ""                                             // 현재 검색어
)