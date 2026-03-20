package com.hsjeong.supporttools

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyboardShortcutGroup
import android.view.Menu
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.hsjeong.supporttools.config.AppSupportConfig
import com.hsjeong.supporttools.ui.main.SupportToolsActivity
import com.hsjeong.supporttools.utils.DeepLinkData
import com.hsjeong.supporttools.utils.DeepLinkManager
import com.hsjeong.supporttools.utils.LogcatOverlayManager
import com.hsjeong.supporttools.utils.PreferencesUtil
import com.hsjeong.supporttools.utils.ScreenNameOverlayManager
import com.hsjeong.supporttools.utils.UrlConfigData
import com.hsjeong.supporttools.utils.UrlConfigManager
import com.hsjeong.supporttools.utils.WindowLogManager
import okhttp3.OkHttpClient

/**
 * [SupportTools] - 앱 개발 및 테스트를 위한 통합 디버깅 도구 모음
 * * ## 포함된 도구
 * - [UrlConfigManager] : 네트워크 인터셉터 및 서버 설정 (Chucker 라이브러리 필요)
 * - [ScreenNameOverlayManager] : 현재 액티비티 이름을 화면에 플로팅
 * - [WindowLogManager] : 실시간 로그 스택 오버레이
 * - [PreferencesUtil] : PreferenceUtil
 */
object SupportTools {
    private var isDebug = true
    var appSupportConfig: AppSupportConfig? = null
        private set

    @JvmStatic
    @JvmOverloads
    fun initialize(application: Application, debugEnable: Boolean = true, config: AppSupportConfig? = null) {
        isDebug = debugEnable
        appSupportConfig = config
        if (isDebug) {
            config?.let {
                PreferencesUtil.setScreenNameOverLayEnable(application, config.enableScreenNameOverLay)
                PreferencesUtil.setLogcatViewerEnable(application, config.enableLogViewer)
                PreferencesUtil.setNetworkLogEnable(application, config.enableNetworkLog)
                PreferencesUtil.setUrlSwitchingEnable(application, config.enableUrlSwitching)
            }

            ProcessLifecycleOwner.get().lifecycle.addObserver(object :
                DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    if (PreferencesUtil.getLogcatViewerEnable(application)) {
                        LogcatOverlayManager.show(application)
                    }
                }

                override fun onStop(owner: LifecycleOwner) {
                    LogcatOverlayManager.remove()
                    WindowLogManager.remove()
                }
            })

            application.registerActivityLifecycleCallbacks(object :
                Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivityResumed(activity: Activity) {
                    if (appSupportConfig == null) {
                        val window = activity.window
                        if (window.callback !is DebugKeyCallback) {
                            val originCallback = window.callback
                            window.callback = DebugKeyCallback(originCallback, activity)
                        }
                    }
                }
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityDestroyed(activity: Activity) {}
            })

            // 화면 액티비티명 노출 설정
            ScreenNameOverlayManager.initialize(application)
        }
    }

    // 서버 설정을 위한 url 값 설정
    @JvmStatic
    fun setUrlConfigData(application: Application, list: List<UrlConfigData>, callback: ((targetUrlsMap: Map<String, String>) -> Unit)? = null) {
        UrlConfigManager.setUrlConfigData(list) {
            val isNetworkSwitching = PreferencesUtil.getUrlSwitchingEnable(application)
            if (isNetworkSwitching) {
                val targetUrlsMap = UrlConfigManager.getTargetUrlsMap(application)
                if (!targetUrlsMap.isEmpty() && list.size == targetUrlsMap.size) {
                    callback?.invoke(targetUrlsMap)
                    Toast.makeText(application, "${UrlConfigManager.getServerType(application)} 설정", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 네트워크 및 서버 설정 (Chucker 및 서버 환경 변경)
    @JvmStatic
    fun addNetworkInterceptor(
        context: Context,
        okHttpBuilder: OkHttpClient.Builder
    ) {
        if (!isDebug) {
            return
        }

        val isNetworkLog = PreferencesUtil.getNetworkLogEnable(context)
        val isNetworkSwitching = PreferencesUtil.getUrlSwitchingEnable(context)

        if (isNetworkSwitching) {
            okHttpBuilder.addInterceptor(UrlConfigManager.UrlSwitchingInterceptor(context))
        }

        if (isNetworkLog) {
            // 1. ChuckerCollector 생성 (데이터 수집기)
            val collector = ChuckerCollector(
                context,
                true,  // 알림 표시 여부
                RetentionManager.Period.ONE_DAY // 로그 유지 기간
            )

            // 2. ChuckerInterceptor 생성
            val chuckerInterceptor = ChuckerInterceptor.Builder(context)
                .collector(collector)
                .maxContentLength(250000L)
                .alwaysReadResponseBody(true)
                .build()
            okHttpBuilder.addInterceptor(chuckerInterceptor)
        }
    }

    @JvmStatic
    fun showSupportToolsUi(activity: Activity) {
        SupportToolsActivity.start(activity)
    }

    // 딥링크 테스터에서 보여줄 규격 리스트를 설정
    @JvmStatic
    fun setDeepLinkData(list: List<DeepLinkData>? = null) {
        if (list.isNullOrEmpty()) {
            return
        }
        DeepLinkManager.setDeepLinkList(list)
    }

    private class DebugKeyCallback(private val origin: Window.Callback, private val activity: Activity) : Window.Callback by origin {
        private var isVolumeUpPressed = false
        private var isVolumeDownPressed = false
        private var isTriggered = false

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            val keyCode = event.keyCode
            val action = event.action

            if (action == KeyEvent.ACTION_DOWN && event.repeatCount > 0) {
                return origin.dispatchKeyEvent(event)
            }

            when (action) {
                KeyEvent.ACTION_DOWN -> {
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) isVolumeUpPressed = true
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) isVolumeDownPressed = true
                }

                KeyEvent.ACTION_UP -> {
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) isVolumeUpPressed = false
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) isVolumeDownPressed = false

                    if (!isVolumeUpPressed || !isVolumeDownPressed) {
                        isTriggered = false
                    }
                }
            }

            if (isVolumeUpPressed && isVolumeDownPressed && !isTriggered) {
                isTriggered = true
                SupportToolsActivity.start(activity)
                return true
            }
            return origin.dispatchKeyEvent(event)
        }

        override fun onProvideKeyboardShortcuts(
            data: List<KeyboardShortcutGroup?>?,
            menu: Menu?,
            deviceId: Int
        ) {
            origin.onProvideKeyboardShortcuts(data, menu, deviceId)
        }

        override fun onPointerCaptureChanged(hasCapture: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                origin.onPointerCaptureChanged(hasCapture)
            }
        }
    }
}