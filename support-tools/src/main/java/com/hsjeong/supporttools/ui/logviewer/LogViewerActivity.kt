package com.hsjeong.supporttools.ui.logviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hsjeong.supporttools.ui.base.BaseActivity
import com.hsjeong.supporttools.utils.PreferencesUtil
import kotlin.getValue

class LogViewerActivity : BaseActivity() {
    companion object {
        fun startActivityNewTask(context: Context) {
            val intent = Intent(context, LogViewerActivity::class.java).apply {
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }

    private val viewModel: LogViewerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            LogViewerScreen(state = state, onUiEventListener = { event ->
                onUiEvent(event = event)
            })
        }
    }

    private fun init() {
        val searchWord = PreferencesUtil.getLogcatViewerSearchWord(this)
        viewModel.processIntent(LogViewerIntent.LoadLogData(searchWord ?: ""))
    }

    private fun onUiEvent(event: LogViewerUiEvent) {
        when(event) {
            is LogViewerUiEvent.Close -> {
                finish()
            }

            is LogViewerUiEvent.Clear -> {
                viewModel.processIntent(LogViewerIntent.ClearLogData)
            }

            is LogViewerUiEvent.SearchLogData -> {
                PreferencesUtil.setLogcatViewerSearchWord(this, event.searchText)
                viewModel.processIntent(LogViewerIntent.SearchLogData(event.searchText))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}