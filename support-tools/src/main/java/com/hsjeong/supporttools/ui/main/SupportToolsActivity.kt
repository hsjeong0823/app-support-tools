package com.hsjeong.supporttools.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.hsjeong.supporttools.R
import com.hsjeong.supporttools.ui.base.BaseActivity
import com.hsjeong.supporttools.ui.deeplinktester.DeepLinkTesterActivity
import com.hsjeong.supporttools.ui.logviewer.LogViewerActivity
import com.hsjeong.supporttools.ui.preferenceviewer.PreferenceViewerActivity
import com.hsjeong.supporttools.utils.LogcatOverlayManager
import com.hsjeong.supporttools.utils.PreferencesUtil
import com.hsjeong.supporttools.utils.UrlConfigManager
import kotlin.system.exitProcess

class SupportToolsActivity : BaseActivity() {
    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, SupportToolsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
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
                onUiEvent(it)
            })
        }
    }

    private fun init() {
        val showScreenName = PreferencesUtil.getScreenNameOverLayEnable(this)
        val showLogcatViewer = PreferencesUtil.getLogcatViewerEnable(this)
        val showNetworkLog = PreferencesUtil.getNetworkLogEnable(this)
        val enableServerChange = PreferencesUtil.getUrlSwitchingEnable(this)
        val serverType = UrlConfigManager.getServerType(this)
        viewModel.processIntent(
            SupportToolsIntent.Init(
                showScreenName = showScreenName,
                showLogcatViewer = showLogcatViewer,
                showNetworkLog = showNetworkLog,
                enableServerChange = enableServerChange,
                serverType = serverType
            )
        )
    }

    private fun onUiEvent(event: SupportToolsUiEvent) {
        when (event) {
            SupportToolsUiEvent.Close -> {
                finish()
            }

            SupportToolsUiEvent.Apply -> {
                applySetting()
            }

            SupportToolsUiEvent.MovePreferenceViewer -> {
                PreferenceViewerActivity.start(this)
            }

            SupportToolsUiEvent.MoveDeepLinkTester -> {
                DeepLinkTesterActivity.start(this)
            }

            SupportToolsUiEvent.MoveLogViewer -> {
                LogViewerActivity.start(this)
            }
        }
    }

    private fun applySetting() {
        val state = viewModel.state.value
        val oldShowScreenName = PreferencesUtil.getScreenNameOverLayEnable(this)
        val oldShowNetworkLog = PreferencesUtil.getNetworkLogEnable(this)
        val oldEnableServerChange = PreferencesUtil.getUrlSwitchingEnable(this)
        val oldServerType = UrlConfigManager.getServerType(this)

        PreferencesUtil.setScreenNameOverLayEnable(application, state.showScreenName)
        PreferencesUtil.setLogcatViewerEnable(application, state.showLogcatViewer)
        PreferencesUtil.setNetworkLogEnable(application, state.showNetworkLog)
        PreferencesUtil.setUrlSwitchingEnable(application, state.enableServerChange)
        UrlConfigManager.setServerType(application, state.selectedServer)

        val needRestart = oldShowScreenName != state.showScreenName ||
                    oldShowNetworkLog != state.showNetworkLog ||
                    oldEnableServerChange != state.enableServerChange ||
                    oldServerType != state.selectedServer

        if (needRestart) {
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
        } else {
            if (state.showLogcatViewer) {
                LogcatOverlayManager.show(this)
            } else {
                LogcatOverlayManager.remove()
            }
            finish()
        }
    }
}