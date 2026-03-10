package com.hsjeong.supporttools

import android.app.Activity
import android.app.Application
import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.hsjeong.supporttools.ui.SupportToolsActivity
import com.hsjeong.supporttools.utils.ScreenNameOverlayUtil
import com.hsjeong.supporttools.utils.WindowLogUtil
import com.hsjeong.supporttools.utils.UrlConfigUtil
import com.hsjeong.supporttools.utils.PreferencesUtil
import com.hsjeong.supporttools.utils.UrlConfigUtil.UrlConfigData
import okhttp3.OkHttpClient

/**
 * [SupportTools] - 앱 개발 및 테스트를 위한 통합 디버깅 도구 모음
 * * ## 포함된 도구
 * - [UrlConfigUtil] : 네트워크 인터셉터 및 서버 설정 (Chucker 라이브러리 필요)
 * - [ScreenNameOverlayUtil] : 현재 액티비티 이름을 화면에 플로팅
 * - [WindowLogUtil] : 실시간 로그 스택 오버레이
 * - [PreferencesUtil] : PreferenceUtil
 */
object SupportTools {
    private var isDebug = true

    @JvmStatic
    fun initialize(application: Application, debugEnable: Boolean = true) {
        isDebug = debugEnable
        if (isDebug) {
            // 화면 액티비티명 노출 설정
            ScreenNameOverlayUtil.initialize(application)
        }
    }

    // 서버 설정을 위한 url 값 설정
    @JvmStatic
    fun setUrlConfigData(list: List<UrlConfigData>) {
        UrlConfigUtil.setUrlConfigData(list)
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

        val isNetworkLog = PreferencesUtil().getNetworkLogEnable(context)
        val isNetworkSwitching = PreferencesUtil().getUrlSwitchingEnable(context)

        if (isNetworkSwitching) {
            okHttpBuilder.addInterceptor(UrlConfigUtil.UrlSwitchingInterceptor(context))
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
}