package com.hsjeong.supporttools.ui.preferenceviewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.hsjeong.supporttools.utils.PreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PreferenceViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(PreferenceViewerState())
    val state: StateFlow<PreferenceViewerState> = _state

    init {
        processIntent((PreferenceViewerIntent.LoadPreferenceFiles))
    }

    fun processIntent(intent: PreferenceViewerIntent) {
        when (intent) {
            is PreferenceViewerIntent.LoadPreferenceFiles -> {
                val files = PreferencesUtil.getPreferenceFiles(getApplication())
                _state.update {
                    it.copy(
                        preferenceAllFiles = files,
                        preferenceSearchedFiles = files
                    )
                }
            }

            is PreferenceViewerIntent.SearchPreferenceFiles -> {
                _state.update {
                    val files = if (intent.searchText.isEmpty()) {
                        it.preferenceAllFiles
                    } else {
                        it.preferenceAllFiles.filter { file ->
                            file.contains(intent.searchText, ignoreCase = true)
                        }
                    }

                    it.copy(
                        preferenceSearchedFiles = files,
                        searchText = intent.searchText
                    )
                }
            }
        }
    }
}