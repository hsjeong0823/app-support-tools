package com.hsjeong.supporttools.ui.environmentconfig

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hsjeong.supporttools.R
import com.hsjeong.supporttools.ui.base.BaseActivity
import com.hsjeong.supporttools.utils.UrlConfigManager

class EnvironmentConfigViewerActivity : BaseActivity() {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, EnvironmentConfigViewerActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val json = UrlConfigManager.getEnvironmentConfigJson()
        setContent {
            EnvironmentConfigViewerScreen(
                json = json,
                onCloseClick = ::finish,
                onCopyClick = { copyJson(json) },
            )
        }
    }

    private fun copyJson(json: String?) {
        if (json.isNullOrEmpty()) {
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("support_tools_environments.json", json))
        Toast.makeText(this, R.string.environment_config_copied, Toast.LENGTH_SHORT).show()
    }
}
