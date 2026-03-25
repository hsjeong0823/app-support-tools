package com.hsjeong.supporttools

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
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
    private const val TAG = "SupportTools"
    private var isDebug = true
    var appSupportConfig: AppSupportConfig? = null
        private set

    private inline fun runSafe(action: () -> Unit) {
        try {
            action()
        } catch (t: Throwable) {
            Log.e(TAG, "Internal error occurred in SupportTools", t)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun initialize(application: Application, debugEnable: Boolean = true, config: AppSupportConfig? = null) {
        runSafe {
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

                application.registerActivityLifecycleCallbacks(object :
                    Application.ActivityLifecycleCallbacks {
                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                    override fun onActivityResumed(activity: Activity) {
                        runSafe {
                            if (appSupportConfig == null) {
                                val window = activity.window
                                if (window.callback !is DebugKeyCallback) {
                                    val originCallback = window.callback
                                    window.callback = DebugKeyCallback(originCallback, activity)
                                }
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
            } else {
                PreferencesUtil.setScreenNameOverLayEnable(application, false)
                PreferencesUtil.setLogcatViewerEnable(application, false)
                PreferencesUtil.setNetworkLogEnable(application, false)
                PreferencesUtil.setUrlSwitchingEnable(application, false)
            }

            // 테스트용
            setDefaultDeeplinkData()
        }
    }

    // 서버 설정을 위한 url 값 설정
    @JvmStatic
    fun setUrlConfigData(context: Context, list: List<UrlConfigData>, callback: ((targetUrlsMap: Map<String, String>) -> Unit)? = null) {
        runSafe {
            UrlConfigManager.setUrlConfigData(list) {
                runSafe {
                    val isNetworkSwitching = PreferencesUtil.getUrlSwitchingEnable(context)
                    if (isNetworkSwitching) {
                        val targetUrlsMap = UrlConfigManager.getTargetUrlsMap(context)
                        if (!targetUrlsMap.isEmpty() && list.size == targetUrlsMap.size) {
                            callback?.invoke(targetUrlsMap)
                            Toast.makeText(context, "${UrlConfigManager.getServerType(context)} 설정", Toast.LENGTH_LONG).show()
                        }
                    }
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
        runSafe {
            val isNetworkSwitching = PreferencesUtil.getUrlSwitchingEnable(context)

            if (isNetworkSwitching) {
                okHttpBuilder.addInterceptor(UrlConfigManager.UrlSwitchingInterceptor(context))
            }
        }

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

    private class DebugKeyCallback(private val origin: Window.Callback, private val activity: Activity) : Window.Callback by origin {
        private var isVolumeUpPressed = false
        private var isVolumeDownPressed = false
        private var isTriggered = false

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            return try {
                handleKeyEventInternal(event)
            } catch (t: Throwable) {
                Log.e(TAG, "Error in DebugKeyCallback dispatchKeyEvent", t)
                origin.dispatchKeyEvent(event)
            }
        }

        private fun handleKeyEventInternal(event: KeyEvent): Boolean {
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
            try {
                origin.onProvideKeyboardShortcuts(data, menu, deviceId)
            } catch (t: Throwable) {
                Log.e(TAG, "Error in DebugKeyCallback onProvideKeyboardShortcuts", t)
            }
        }

        override fun onPointerCaptureChanged(hasCapture: Boolean) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    origin.onPointerCaptureChanged(hasCapture)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error in DebugKeyCallback onPointerCaptureChanged", t)
            }
        }
    }

    private fun setDefaultDeeplinkData() {
        DeepLinkManager.setDeepLinkList(listOf(
            // 메인 및 회원
            DeepLinkData("홈 화면", "starbuckskr://deeplink?main=HOME"),
            DeepLinkData("회원가입", "starbuckskr://deeplink?main=JOIN"),
            DeepLinkData("로그인", "starbuckskr://deeplink?main=LOGIN"),
            DeepLinkData("알림톡 인증 완료", "starbuckskr://deeplink?main=MESSAGE_AUTH&messageAuthResponseKey=[value]&authenticationYn=[value]"),

            // 리워드 및 쿠폰
            DeepLinkData("리워드 메인", "starbuckskr://deeplink?main=REWARDS"),
            DeepLinkData("별 히스토리", "starbuckskr://deeplink?main=REWARDS&sub=HISTORY"),
            DeepLinkData("리워드 웹뷰", "starbuckskr://deeplink?main=REWARDS_WEB&serviceId=[value]&channel=[value]"),
            DeepLinkData("쿠폰 (사용 가능)", "starbuckskr://deeplink?main=ECOUPON&sub=AVAILABLE"),
            DeepLinkData("쿠폰 등록", "starbuckskr://deeplink?main=ECOUPON&sub=REGIST&couponNum=[value]"),

            // 카드 및 결제 (Pay)
            DeepLinkData("카드 메인 (Pay)", "starbuckskr://deeplink?main=CARD"),
            DeepLinkData("간편결제 - 신용카드", "starbuckskr://deeplink?main=CARD&sub=CREDIT_CARD"),
            DeepLinkData("간편결제 - 계좌", "starbuckskr://deeplink?main=CARD&sub=ACCOUNT"),
            DeepLinkData("자동 충전 설정", "starbuckskr://deeplink?main=CARD&sub=AUTO_RELOAD"),
            DeepLinkData("일반 충전", "starbuckskr://deeplink?main=CARD&sub=RELOAD"),
            DeepLinkData("스타벅스 카드 등록", "starbuckskr://deeplink?main=CARD&sub=REGIST&cardNum=[value]&pinNum=[value]"),
            DeepLinkData("카드 교환권 등록", "starbuckskr://deeplink?main=CARD&sub=REGIST_VOUCHER&giftCardNo=[value]"),
            DeepLinkData("결제수단 관리 - 스벅 카드", "starbuckskr://deeplink?main=CARD_MANAGEMENT&sub=STARBUCKS_CARD"),
            DeepLinkData("결제수단 관리 - 신용 카드", "starbuckskr://deeplink?main=CARD_MANAGEMENT&sub=CREDIT_CARD"),
            DeepLinkData("신용카드 등록", "starbuckskr://deeplink?main=CARD_MANAGEMENT&sub=CREDIT_REGIST"),

            // 사이렌 오더 (Order)
            DeepLinkData("사이렌 오더 메인", "starbuckskr://deeplink?main=SIREN_ORDER"),
            DeepLinkData("음료 카테고리", "starbuckskr://deeplink?main=SIREN_ORDER&sub=DRINK_CATEGORY&categoryCode=[value]"),
            DeepLinkData("푸드 카테고리", "starbuckskr://deeplink?main=SIREN_ORDER&sub=FOOD_CATEGORY&categoryCode=[value]"),
            DeepLinkData("상품 카테고리", "starbuckskr://deeplink?main=SIREN_ORDER&sub=WHOLE_BEAN_CATEGORY&categoryCode=[value]"),
            DeepLinkData("매장 상세", "starbuckskr://deeplink?main=SIREN_ORDER&sub=STORE_DETAIL&storeCd=[value]"),
            DeepLinkData("음료 상세", "starbuckskr://deeplink?main=SIREN_ORDER&sub=DRINK_DETAIL&skuNo=[value]"),
            DeepLinkData("나만의 메뉴", "starbuckskr://deeplink?main=SIREN_ORDER&sub=MY_MENU"),
            DeepLinkData("오더 히스토리 상세", "starbuckskr://deeplink?main=SIREN_HISTORY&sub=DETAIL&orderNo=[value]"),
            DeepLinkData("오더 스테이터스 상세", "starbuckskr://deeplink?main=ORDER_STATUS&sub=DT_ORDER_SEND&orderNo=[value]"),

            // 선물하기 (Gift)
            DeepLinkData("기프트 샵 홈", "starbuckskr://deeplink?main=GIFT_SHOP"),
            DeepLinkData("기프트 카테고리 (전체)", "starbuckskr://deeplink?main=GIFT_SHOP&sub=CATEGORY&gubun=A"),
            DeepLinkData("선물 상세", "starbuckskr://deeplink?main=GIFT_SHOP&sub=GIFT_DETAIL&type=1&productNo=[value]&skuNo=[value]"),
            DeepLinkData("받은 선물함", "starbuckskr://deeplink?main=GIFT_SHOP&sub=RECEIVED_GIFT_BOX"),
            DeepLinkData("모바일 상품권 등록", "starbuckskr://deeplink?main=GIFT_SHOP&sub=REGIST&giftNum=[value]&pin=[value]"),
            DeepLinkData("e-Gift Card 선물하기", "starbuckskr://deeplink?main=EGIFT_CARD_PRESENT"),

            // 고객 지원 및 이용 안내
            DeepLinkData("Inbox (What's New)", "starbuckskr://deeplink?main=WHATS_NEW"),
            DeepLinkData("Inbox 상세", "starbuckskr://deeplink?main=WHATS_NEW&sub=DETAIL&url=[value]"),
            DeepLinkData("공지사항", "starbuckskr://deeplink?main=USER_GUIDE&sub=NOTICE"),
            DeepLinkData("자주 하는 질문 (FAQ)", "starbuckskr://deeplink?main=USER_GUIDE&sub=FAQ"),
            DeepLinkData("이용 약관", "starbuckskr://deeplink?main=USER_GUIDE&sub=TERMS_OF_USE"),
            DeepLinkData("개인정보 처리 방침", "starbuckskr://deeplink?main=USER_GUIDE&sub=PRIVACY_POLICY"),
            DeepLinkData("고객 센터", "starbuckskr://deeplink?main=CUSTOMER_SERVICE"),

            // 기타 서비스
            DeepLinkData("프리퀀시", "starbuckskr://deeplink?main=EFREQUENCY"),
            DeepLinkData("마이 스타벅스 리뷰", "starbuckskr://deeplink?main=MYSTARBUCKS_REVIEW"),
            DeepLinkData("럭키 드로우 (스크래치)", "starbuckskr://deeplink?main=DIGITAL_LUCKY_DRAW"),
            DeepLinkData("개인컵 리워드 설정", "starbuckskr://deeplink?main=SETTING&sub=TUMBLER_REWARD_SETTING"),
            DeepLinkData("My DT Pass 설정", "starbuckskr://deeplink?main=SETTING&sub=MY_DT_PASS"),
            DeepLinkData("Delivers 메뉴 상세", "starbuckskr://deeplink?main=ONDEMAND&sub=MENU_DETAIL&skuNo=[value]&storeCode=[value]"),
            DeepLinkData("개인정보 관리", "starbuckskr://deeplink?main=PERSONAL_INFO"),
            DeepLinkData("비밀번호 변경", "starbuckskr://deeplink?main=PASSWORD_CHANGE"),

            // 이벤트/제휴
            DeepLinkData("통합멤버십", "starbuckskr://deeplink?main=MEMBERSHIP&rcmndCode=[value]"),
            DeepLinkData("T우주패스", "starbuckskr://deeplink?main=SKT_UNIVERSE"),
            DeepLinkData("PLCC 프로모션", "starbuckskr://deeplink?main=PLCC&sub=PROMOTION&eventNo=[value]"),
            DeepLinkData("간편결제 CDD 재이행", "starbuckskr://deeplink?main=SIMPLEPAY_CDD&cddAuthKey=[value]"),
            DeepLinkData("라이브 오더 진행 상태", "starbuckskr://deeplink?main=LIVE_PROGRESS&orderNo=[value]&isReserv=[value]&step=[value]")
        ))
    }
}