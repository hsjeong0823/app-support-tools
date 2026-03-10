package com.hsjeong.supporttools.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.hsjeong.supporttools.R
import com.hsjeong.supporttools.ui.base.BaseActivity
import com.hsjeong.supporttools.utils.PreferencesUtil
import com.hsjeong.supporttools.utils.UrlConfigUtil
import kotlin.system.exitProcess

class SupportToolsActivity : BaseActivity() {
    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, SupportToolsActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private val viewModel: SupportToolsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        init()
        setContent {
            SupportToolsScreen(viewModel = viewModel, onUiEventListener = {
                when (it) {
                    SupportToolsUiEvent.Close -> {
                        finish()
                    }

                    SupportToolsUiEvent.Apply -> {
                        applySetting()
                    }
                }
            })
        }
    }

    private fun init() {
        val showScreenName = PreferencesUtil().getScreenNameOverLayEnable(this)
        val showNetworkLog = PreferencesUtil().getNetworkLogEnable(this)
        val enableServerChange = PreferencesUtil().getUrlSwitchingEnable(this)
        val serverType = UrlConfigUtil.getServerType(this)
        viewModel.processIntent(
            SupportToolsIntent.Init(
                showScreenName = showScreenName,
                showNetworkLog = showNetworkLog,
                enableServerChange = enableServerChange,
                serverType = serverType
            )
        )
    }

    private fun applySetting() {
        val state = viewModel.state.value
        PreferencesUtil().setScreenNameOverLayEnable(application, state.showScreenName)
        PreferencesUtil().setNetworkLogEnable(application, state.showNetworkLog)
        PreferencesUtil().setUrlSwitchingEnable(application, state.enableServerChange)
        UrlConfigUtil.setServerType(application, state.selectedServer)

        val restartApp = {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            finishAffinity()
            startActivity(intent)
            exitProcess(0)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.alert)
            .setMessage(R.string.alert_restart_message)
            .setPositiveButton(R.string.confirm) { dialog, which ->
                restartApp()
            }
            .setOnCancelListener {
                restartApp()
            }
            .show()
    }
}