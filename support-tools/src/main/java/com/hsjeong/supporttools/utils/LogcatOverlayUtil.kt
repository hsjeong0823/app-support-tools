package com.hsjeong.supporttools.utils

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import com.hsjeong.supporttools.ui.logviewer.LogViewerActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference

object LogcatOverlayUtil {
    private var windowManagerRef: WeakReference<WindowManager>? = null
    private var buttonViewRef: WeakReference<ImageView>? = null

    private fun createLayoutParams(context: Context): WindowManager.LayoutParams {
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }
    }

    internal fun show(context: Context?) {
        if (context == null || !PermissionUtil.checkOverlayPermission(context)) {
            return
        }

        val appContext = context.applicationContext

        if (buttonViewRef?.get() != null) {
            remove()
        }

        val wm = windowManagerRef?.get() ?: (appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).also {
            windowManagerRef = WeakReference(it)
        }

        var button = buttonViewRef?.get()
        if (button == null) {
            val buttonImageView = ImageView(context).apply {
                setImageResource(android.R.drawable.ic_menu_info_details)
                setOnClickListener {
                    LogViewerActivity.startActivityNewTask(context)
                }
            }
            button = buttonImageView.also { buttonViewRef = WeakReference(it) }
        }

        if (button.parent == null) {
            try {
                wm.addView(button, createLayoutParams(appContext))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    internal fun remove() {
        try {
            val wm = windowManagerRef?.get()
            val button = buttonViewRef?.get()
            if (wm != null && button != null && button.parent != null) {
                wm.removeView(button)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 모든 참조 명시적 제거
            buttonViewRef?.clear()
        }
    }

    enum class LogLevel {
        VERBOSE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        UNKNOWN
    }

    data class LogItemData(
        val log: String,
        val level: LogLevel
    )

    internal fun parseLogLevel(log: String): LogLevel {
        return when {
            log.contains(" V/") -> LogLevel.VERBOSE
            log.contains(" D/") -> LogLevel.DEBUG
            log.contains(" I/") -> LogLevel.INFO
            log.contains(" W/") -> LogLevel.WARN
            log.contains(" E/") -> LogLevel.ERROR
            else -> LogLevel.UNKNOWN
        }
    }

    internal fun logColor(level: LogLevel): Int {
        return when (level) {
            LogLevel.VERBOSE -> Color.GRAY
            LogLevel.DEBUG -> Color.WHITE
            LogLevel.INFO -> Color.GREEN
            LogLevel.WARN -> Color.YELLOW
            LogLevel.ERROR -> Color.RED
            LogLevel.UNKNOWN -> Color.LTGRAY
        }
    }

    /**
     * 로그 읽기
     */
    internal fun getLogs(): List<LogItemData> {
        val logs = mutableListOf<LogItemData>()
        try {
            val pid = android.os.Process.myPid()
            val commandArray = mutableListOf(
                "logcat",
                "--pid=$pid",
                "-v",
                "time",
                "-d",
                "-t",
                "1000"
            )

            val process = ProcessBuilder()
                .command(commandArray)
                .start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val log = line ?: ""
                val level = parseLogLevel(log)
                logs.add(LogItemData(log = log, level = level))
            }
        } catch (e: Exception) {
            logs.add(LogItemData(log = "Log read error : ${e.message}", level = LogLevel.ERROR))
        }
        return logs
    }

    /**
     * 로그 초기화
     */
    internal fun clearLogs() {
        Runtime.getRuntime().exec("logcat -c")
    }
}