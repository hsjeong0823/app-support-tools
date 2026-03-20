package com.hsjeong.supporttools.ui.deeplinktester

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.hsjeong.supporttools.ui.base.BaseActivity
import androidx.core.net.toUri
import com.hsjeong.supporttools.utils.PreferencesUtil

class DeepLinkTesterActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DeepLinkTesterActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val viewModel: DeepLinkTesterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContent {
            DeepLinkTesterScreen(viewModel = viewModel) { event ->
                onUiEvent(event)
            }
        }
    }

    private fun init() {
        PreferencesUtil.getDeeplinkSearchUrl(this)?.let {
            viewModel.updateUri(it)
        }
    }

    private fun onUiEvent(event: DeepLinkTesterUiEvent) {
        when (event) {
            is DeepLinkTesterUiEvent.Close -> {
                finish()
            }
            is DeepLinkTesterUiEvent.DeeplinkInputText -> {
                viewModel.updateUri(event.inputText)
            }
            is DeepLinkTesterUiEvent.DeeplinkLaunch -> {
                PreferencesUtil.setDeeplinkSearchUrl(this, event.uri)
                launchDeepLink(event.uri)
            }
            is DeepLinkTesterUiEvent.CopyToClipboard -> {
//                copyToClipboard(event.uri)
                viewModel.updateUri(event.uri)
            }
        }
    }

    private fun launchDeepLink(uriString: String) {
        try {
            // 동일한 Task 내에서 실행되도록 FLAG_ACTIVITY_NEW_TASK 생략
            val intent = Intent(Intent.ACTION_VIEW, uriString.toUri())
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "잘못된 URI이거나 처리할 수 없는 링크입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("DeepLink", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }
}