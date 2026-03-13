package com.hsjeong.supporttools.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.isVisible
import java.lang.ref.WeakReference

object WindowLogUtil {
    // 로그 모드 정의
    enum class LogMode {
        SINGLE_LINE, // 단순 현재 화면 이름 (한 줄, 터치 차단)
        MULTI_LINE   // 상세 로그 스택 (200dp, 스크롤 가능, 터치 허용)
    }

    private const val MAX_LINE_COUNT = 100      // 최대 유지할 로그 줄 수
    private const val MAX_SCROLL_HEIGHT = 200f  // 최대 스크롤뷰 영역
    private var currentMode: LogMode = LogMode.SINGLE_LINE

    // 메모리 누수 방지를 위해 WeakReference 사용
    private var windowManagerRef: WeakReference<WindowManager>? = null
    private var rootViewRef: WeakReference<LinearLayout>? = null
    private var logTextViewRef: WeakReference<TextView>? = null
    private var scrollViewRef: WeakReference<ScrollView>? = null

    private fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
    }

    private fun createLayoutParams(context: Context, mode: LogMode): WindowManager.LayoutParams {
        // 모드에 따른 높이 및 플래그 설정
        val height = if (mode == LogMode.MULTI_LINE) dpToPx(context, MAX_SCROLL_HEIGHT) else WindowManager.LayoutParams.WRAP_CONTENT

        // SINGLE_LINE은 터치를 통과
        val flags = if (mode == LogMode.SINGLE_LINE) {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        } else {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // 스크롤을 위해 터치는 허용
        }

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            height,
            windowType,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
        }
    }

    private fun checkLineCount(textView: TextView) {
        if (textView.lineCount >= MAX_LINE_COUNT) {
            val currentText = textView.text.toString()
            val firstNewLineIndex = currentText.indexOf("\n")
            if (firstNewLineIndex != -1) {
                // 가장 오래된 첫 번째 줄 삭제
                textView.text = currentText.substring(firstNewLineIndex + 1)
            }
        }
    }

    private fun updateText(msg: String?, mode: LogMode) {
        val tv = logTextViewRef?.get() ?: return
        val scroll = scrollViewRef?.get()

        if (mode == LogMode.SINGLE_LINE) {
            tv.text = msg
        } else {
            // 줄 수 체크 후 추가
            checkLineCount(tv)
            tv.append("- $msg\n")

            scroll?.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    @JvmStatic
    fun showWindowStatus(context: Context?, msg: String?, mode: LogMode = LogMode.SINGLE_LINE) {
        if (context == null || !PermissionUtil.checkOverlayPermission(context)) {
            return
        }

        val appContext = context.applicationContext

        if (currentMode != mode && rootViewRef?.get() != null) {
            remove()
        }
        currentMode = mode

        val wm = windowManagerRef?.get() ?: (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).also {
            windowManagerRef = WeakReference(it)
        }

        var root = rootViewRef?.get()
        if (root == null) {
            root = initView(appContext).also { rootViewRef = WeakReference(it) }
        }

        if (root.parent == null) {
            try {
                wm.addView(root, createLayoutParams(appContext, mode))
            } catch (e: Exception) { e.printStackTrace() }
        }

        updateText(msg, mode)
    }

    private fun initView(context: Context): LinearLayout {
        val rootView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor("#AA000000".toColorInt())
        }

        val scrollView = ScrollView(context).apply {
            isFillViewport = true
            scrollViewRef = WeakReference(this)
        }

        val textView = TextView(context).apply {
            setTextColor(Color.WHITE)
            textSize = 12f
            setPadding(20, 10, 20, 10)
            logTextViewRef = WeakReference(this)
        }

        scrollView.addView(textView)
        rootView.addView(scrollView)
        return rootView
    }

    fun setViewVisibility(isVisible: Boolean) {
        rootViewRef?.get()?.isVisible = isVisible
    }

    fun remove() {
        try {
            val wm = windowManagerRef?.get()
            val root = rootViewRef?.get()
            if (wm != null && root != null && root.parent != null) {
                wm.removeView(root)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 모든 참조 명시적 제거
            rootViewRef?.clear()
            logTextViewRef?.clear()
            scrollViewRef?.clear()
        }
    }
}