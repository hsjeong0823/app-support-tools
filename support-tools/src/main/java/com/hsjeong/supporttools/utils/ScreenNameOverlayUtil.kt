package com.hsjeong.supporttools.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

// ==========================================
// ScreenNameOverlayUtil (현재 액티비티 이름을 화면에 플로팅 위한 Util)
// - 권한: manifest에 권한 추가 필요 => <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
// - 초기화: Application.onCreate()에서 DebugTools.ScreenNameOverlayUtil.initialize()
// - 라이브러리 추가 필요 : implementation 'androidx.lifecycle:lifecycle-process:2.10.0'
// ==========================================
object ScreenNameOverlayUtil {
    private val fragmentCallback = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentResumed(fm: FragmentManager, fragment: Fragment) {
            super.onFragmentResumed(fm, fragment)
            // 팝업 제외 (BottomSheet는 노출)
            if (fragment is DialogFragment) return

            val activityName = fragment.activity?.javaClass?.simpleName ?: "null"
            val fragmentName = fragment::class.java.simpleName ?: "null"
            updateLog(fragment.activity, "$activityName > $fragmentName")
        }
    }

    @JvmStatic
    fun initialize(application: Application) {
        val isShowScreenName = PreferencesUtil.getScreenNameOverLayEnable(application)
        if (!isShowScreenName) {
            return
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(object :
            DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
            }

            override fun onStop(owner: LifecycleOwner) {
                WindowLogUtil.remove()
            }
        })

        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                (activity as? FragmentActivity)?.supportFragmentManager
                    ?.registerFragmentLifecycleCallbacks(fragmentCallback, true)
            }

            override fun onActivityResumed(activity: Activity) {
                updateLog(activity, "${activity::class.java.simpleName}")
            }
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }

    private fun updateLog(context: Context?, message: String?) {
        context?.let {
            val isShowScreenName = PreferencesUtil.getScreenNameOverLayEnable(context)
            if (!isShowScreenName) {
                WindowLogUtil.remove()
                return
            }
            WindowLogUtil.showWindowStatus(context, message)
        }
    }
}