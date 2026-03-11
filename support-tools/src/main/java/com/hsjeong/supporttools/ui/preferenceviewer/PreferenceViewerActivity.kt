package com.hsjeong.supporttools.ui.preferenceviewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hsjeong.supporttools.ui.base.BaseActivity
import com.hsjeong.supporttools.ui.preferenceviewer.PreferenceViewerIntent.*
import com.hsjeong.supporttools.ui.preferenceviewer.detail.PreferenceViewerDetailActivity
import kotlin.getValue

class PreferenceViewerActivity : BaseActivity() {
    companion object {
        fun start(activity: BaseActivity) {
            val intent = Intent(activity, PreferenceViewerActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private val viewModel: PreferenceViewerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()

            PreferenceViewerScreen(state = state, onUiEventListener = {
                onUiEvent(it)
            })
        }
    }

    private fun init() {
        viewModel.processIntent(SearchPreferenceFiles(packageName))
    }

    private fun onUiEvent(event: PreferenceViewerUiEvent) {
        when (event) {
            is PreferenceViewerUiEvent.Close -> {
                finish()
            }

            is PreferenceViewerUiEvent.SearchPreferenceFiles -> {
                viewModel.processIntent(SearchPreferenceFiles(event.searchText))
            }

            is PreferenceViewerUiEvent.FileSelect -> {
                PreferenceViewerDetailActivity.start(this, PreferenceViewerDetailActivity.ArgInfo(fileName = event.fileName))
            }
        }
    }
}