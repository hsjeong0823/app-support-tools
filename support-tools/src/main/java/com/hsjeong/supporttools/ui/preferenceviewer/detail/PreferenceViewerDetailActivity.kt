package com.hsjeong.supporttools.ui.preferenceviewer.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hsjeong.supporttools.ui.base.BaseActivity
import java.io.Serializable
import kotlin.getValue

class PreferenceViewerDetailActivity : BaseActivity() {
    companion object {
        fun start(activity: BaseActivity, argInfo: ArgInfo) {
            val intent = Intent(activity, PreferenceViewerDetailActivity::class.java).apply {
                this.putExtra(EXTRA_ACTIVITY_INFO, argInfo)
            }
            activity.startActivity(intent)
        }
    }

    data class ArgInfo(
        @JvmField var fileName: String? = null
    ) : Serializable

    private val viewModel: PreferenceViewerDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val argInfo = intent.getIntentSerializableExtra<ArgInfo>(EXTRA_ACTIVITY_INFO)
        argInfo?.let {
            init(it)
            setContent {
                val state by viewModel.state.collectAsStateWithLifecycle()
                PreferenceViewerDetailScreen(state = state, onUiEventListener = { event ->
                    onUiEvent(event)
                })
            }
        } ?: run {
            finish()
        }
    }

    private fun init(argInfo: ArgInfo) {
        argInfo.fileName?.let {
            viewModel.processIntent(PreferenceViewerDetailIntent.LoadPreferenceItems(it))
        }
    }

    private fun onUiEvent(event: PreferenceViewerDetailUiEvent) {
        when (event) {
            is PreferenceViewerDetailUiEvent.Close -> {
                finish()
            }

            is PreferenceViewerDetailUiEvent.SearchPreferenceItems -> {
                viewModel.processIntent(PreferenceViewerDetailIntent.SearchPreferenceItems(event.searchText))
            }

            is PreferenceViewerDetailUiEvent.ModifyPreference -> {
                viewModel.processIntent(PreferenceViewerDetailIntent.ModifyPreference(event.fileName, event.preferenceItem, event.newValue))
            }
        }
    }
}