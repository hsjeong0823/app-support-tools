package com.hsjeong.supporttools.ui.preferenceviewer.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hsjeong.supporttools.utils.PreferencesUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreferenceViewerDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(PreferenceViewerDetailState())
    val state: StateFlow<PreferenceViewerDetailState> = _state

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
                            item.key.contains(intent.searchText, ignoreCase = true)
                        }
                    }

                    it.copy(
                        preferenceSearchedItems = items,
                        searchText = intent.searchText
                    )
                }
            }

            is PreferenceViewerDetailIntent.ModifyPreference -> {
                viewModelScope.launch(Dispatchers.IO) { // 비동기 처리
                    PreferencesUtil.updatePref(getApplication(), intent.fileName, intent.preferenceItem.key, intent.newValue, intent.preferenceItem.type)

                    // 데이터 로드 및 상태 업데이트
                    val items = PreferencesUtil.getAllPrefs(getApplication(), intent.fileName)

                    // 수정 후 리스트 갱신
                    withContext(Dispatchers.Main) {
                        _state.update {
                            val filtered = if (it.searchText.isEmpty()) items
                            else items.filter { item -> item.key.contains(it.searchText) }
                            it.copy(preferenceItems = items, preferenceSearchedItems = filtered)
                        }
                    }
                }
            }
        }
    }
}