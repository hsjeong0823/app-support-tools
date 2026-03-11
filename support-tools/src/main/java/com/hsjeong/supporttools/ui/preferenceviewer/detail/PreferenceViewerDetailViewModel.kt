package com.hsjeong.supporttools.ui.preferenceviewer.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hsjeong.supporttools.utils.PreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class PreferenceViewerDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(PreferenceViewerDetailState())
    val state = _state

    fun processIntent(intent: PreferenceViewerDetailIntent) {
        when (intent) {
            is PreferenceViewerDetailIntent.LoadPreferenceItems -> {
                val items = PreferencesUtil.getAllPrefs(getApplication(), intent.fileName)
                _state.update {
                    it.copy(
                        preferenceItems = items,
                        preferenceSearchedItems = items,
                        fileName = intent.fileName
                    )
                }
            }
            is PreferenceViewerDetailIntent.SearchPreferenceItems -> {
                _state.update {
                    val items = if (intent.searchText.isEmpty()) {
                        it.preferenceItems
                    } else {
                        it.preferenceItems.filter { item ->
                            item.key.contains(intent.searchText)
                        }
                    }

                    it.copy(
                        preferenceSearchedItems = items,
                        searchText = intent.searchText
                    )
                }
            }

            is PreferenceViewerDetailIntent.ModifyPreference -> {
                PreferencesUtil.updatePref(getApplication(), intent.fileName, intent.preferenceItem.key, intent.newValue, intent.preferenceItem.type)

                // 수정 후 리스트 갱신을 위해 재호출
                processIntent(PreferenceViewerDetailIntent.LoadPreferenceItems(intent.fileName))
                if (_state.value.searchText.isNotEmpty()) {
                    processIntent(PreferenceViewerDetailIntent.SearchPreferenceItems(_state.value.searchText))
                }
            }
        }
    }
}