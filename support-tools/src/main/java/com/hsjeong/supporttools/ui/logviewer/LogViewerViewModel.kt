package com.hsjeong.supporttools.ui.logviewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hsjeong.supporttools.utils.LogcatOverlayUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(LogViewerState())
    val state: StateFlow<LogViewerState> = _state

    fun processIntent(intent: LogViewerIntent) {
        when (intent) {
            is LogViewerIntent.LoadLogData -> {
                viewModelScope.launch(Dispatchers.IO) { // 비동기 처리
                    val logData = LogcatOverlayUtil.getLogs()
                    val searchedLogData = if (intent.searchText.isEmpty()) {
                        logData
                    } else {
                        logData.filter { logData ->
                            logData.log.contains(intent.searchText, ignoreCase = true)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        _state.update {
                            it.copy(
                                logData = logData,
                                searchedLogData = searchedLogData,
                                searchText = intent.searchText
                            )
                        }
                    }
                }
            }

            is LogViewerIntent.ClearLogData -> {
                viewModelScope.launch(Dispatchers.IO) {
                    LogcatOverlayUtil.clearLogs()
                    withContext(Dispatchers.Main) {
                        processIntent(LogViewerIntent.LoadLogData(_state.value.searchText))
                    }
                }
            }

            is LogViewerIntent.SearchLogData -> {
                _state.update {
                    val searchedLogData = if (intent.searchText.isEmpty()) {
                        it.logData
                    } else {
                        it.logData.filter { logData ->
                            logData.log.contains(intent.searchText, ignoreCase = true)
                        }
                    }

                    it.copy(
                        searchedLogData = searchedLogData,
                        searchText = intent.searchText
                    )
                }
            }
        }
    }
}