package com.hsjeong.supporttools

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.hsjeong.supporttools.config.AppSupportConfig
import com.hsjeong.supporttools.startup.InitializationGate
import com.hsjeong.supporttools.ui.main.SupportToolsActivity
import com.hsjeong.supporttools.utils.DeepLinkData
import com.hsjeong.supporttools.utils.DeepLinkManager
import com.hsjeong.supporttools.utils.LogcatOverlayManager
import com.hsjeong.supporttools.utils.PreferencesUtil
import com.hsjeong.supporttools.utils.ScreenNameOverlayManager
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
    private const val TAG = "SupportTools"
    private val processInitializationGate = InitializationGate()
    private val screenOverlayInitializationGate = InitializationGate()
    var appSupportConfig: AppSupportConfig? = null
        private set

    internal fun isInitializedForTests(): Boolean =
        processInitializationGate.isInitialized &&
                screenOverlayInitializationGate.isInitialized

    private inline fun runSafe(action: () -> Unit) {
        try {
            action()
        } catch (t: Throwable) {
            Log.e(TAG, "Internal error occurred in SupportTools", t)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun initialize(
        application: Application,
        debugEnable: Boolean = true,
        config: AppSupportConfig? = null
    ) {
        runSafe {
            appSupportConfig = config
            if (debugEnable) {
                config?.let {
                    PreferencesUtil.setScreenNameOverLayEnable(application, config.enableScreenNameOverLay)
                    PreferencesUtil.setLogcatViewerEnable(application, config.enableLogViewer)
                    PreferencesUtil.setNetworkLogEnable(application, config.enableNetworkLog)
                    PreferencesUtil.setUrlSwitchingEnable(application, config.enableUrlSwitching)
                }

                processInitializationGate.runOnce {
                    ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onStart(owner: LifecycleOwner) {
                            runSafe {
                                if (PreferencesUtil.getLogcatViewerEnable(application)) {
                                    LogcatOverlayManager.show(application)
                                }
                            }
                        }

                        override fun onStop(owner: LifecycleOwner) {
                            runSafe {
                                LogcatOverlayManager.remove()
                                WindowLogManager.remove()
                            }
                        }
                    })
                }

                // 화면 액티비티명 노출 설정
                screenOverlayInitializationGate.runOnce {
                    ScreenNameOverlayManager.initialize(application)
                }
            } else {
                PreferencesUtil.setScreenNameOverLayEnable(application, false)
                PreferencesUtil.setLogcatViewerEnable(application, false)
                PreferencesUtil.setNetworkLogEnable(application, false)
                PreferencesUtil.setUrlSwitchingEnable(application, false)
            }
        }
    }

    @JvmStatic
    fun loadEnvironmentConfig(context: Context, @RawRes resourceId: Int): Boolean =
        try {
            context.resources.openRawResource(resourceId).bufferedReader().use {
                UrlConfigManager.installEnvironmentConfig(it.readText())
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load environment config", t)
            UrlConfigManager.clearEnvironmentConfig()
            false
        }

    @JvmStatic
    fun resolveAuthority(context: Context, originalAuthority: String): String =
        try {
            UrlConfigManager.resolveAuthority(context.applicationContext, originalAuthority)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to resolve authority", t)
            originalAuthority
        }

    // 네트워크 및 서버 설정 (Chucker 및 서버 환경 변경)
    @JvmStatic
    fun addNetworkInterceptor(
        context: Context,
        okHttpBuilder: OkHttpClient.Builder
    ) {
        runSafe {
            val isNetworkLog = PreferencesUtil.getNetworkLogEnable(context)
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
    }

    @JvmStatic
    fun showSupportToolsUi(activity: Activity) {
        runSafe {
            SupportToolsActivity.start(activity)
        }
    }

    // 딥링크 테스터에서 보여줄 규격 리스트를 설정
    @JvmStatic
    fun setDeepLinkData(list: List<DeepLinkData>? = null) {
        if (list.isNullOrEmpty()) {
            return
        }
        runSafe {
            DeepLinkManager.setDeepLinkList(list)
        }
    }
}
