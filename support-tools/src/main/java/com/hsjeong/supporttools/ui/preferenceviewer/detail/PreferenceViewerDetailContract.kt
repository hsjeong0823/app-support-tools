package com.hsjeong.supporttools.ui.preferenceviewer.detail

import com.hsjeong.supporttools.utils.PreferenceItem

// Activity로 전달되는 이벤트 정의
sealed class PreferenceViewerDetailUiEvent {
    data object Close : PreferenceViewerDetailUiEvent()
    data class SearchPreferenceItems(val searchText: String) : PreferenceViewerDetailUiEvent()
    data class ModifyPreference(val fileName: String, val preferenceItem: PreferenceItem, val newValue: Any) : PreferenceViewerDetailUiEvent()
}

// ViewModel로 전달되는 Intent 정의
sealed class PreferenceViewerDetailIntent {
    data class LoadPreferenceItems(val fileName: String) : PreferenceViewerDetailIntent()
    data class SearchPreferenceItems(val searchText: String) : PreferenceViewerDetailIntent()
    data class ModifyPreference(val fileName: String, val preferenceItem: PreferenceItem, val newValue: Any) : PreferenceViewerDetailIntent()
}

// Compose에서 사용되는 State 정의
data class PreferenceViewerDetailState(
    val preferenceItems: List<PreferenceItem> = emptyList(),
    val preferenceSearchedItems: List<PreferenceItem> = emptyList(),
    val searchText: String = "",
    val fileName: String = ""
)