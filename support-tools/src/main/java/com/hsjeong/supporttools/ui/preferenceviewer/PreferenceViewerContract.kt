package com.hsjeong.supporttools.ui.preferenceviewer

// Activity로 전달되는 이벤트 정의
sealed class PreferenceViewerUiEvent {
    data object Close : PreferenceViewerUiEvent()
    data class SearchPreferenceFiles(val searchText: String) : PreferenceViewerUiEvent()
    data class FileSelect(val fileName: String) : PreferenceViewerUiEvent()
}

// ViewModel로 전달되는 Intent 정의
sealed class PreferenceViewerIntent {
    data object LoadPreferenceFiles : PreferenceViewerIntent()
    data class SearchPreferenceFiles(val searchText: String) : PreferenceViewerIntent()
}

// Compose에서 사용되는 State 정의
data class PreferenceViewerState(
    val preferenceAllFiles: List<String> = emptyList(),
    val preferenceSearchedFiles: List<String> = emptyList(), // 화면에 보일 필터링된 데이터
    val searchText: String = ""                              // 현재 검색어
)