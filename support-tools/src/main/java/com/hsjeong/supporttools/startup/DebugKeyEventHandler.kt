package com.hsjeong.supporttools.startup

import android.view.KeyEvent

internal data class DebugKeyEvent(val action: Int, val keyCode: Int, val repeatCount: Int = 0)
internal enum class DebugKeyEventDecision { FORWARD, CONSUME }
internal class DebugKeyEventHandler(
    private val isEnabled: () -> Boolean,
    private val onTriggered: () -> Unit,
) {
    private var isVolumeUpPressed = false
    private var isVolumeDownPressed = false
    private var isTriggered = false

    fun handle(event: DebugKeyEvent): DebugKeyEventDecision {
        if (!isEnabled()) {
            isVolumeUpPressed = false
            isVolumeDownPressed = false
            isTriggered = false
            return DebugKeyEventDecision.FORWARD
        }

        val keyCode = event.keyCode
        val action = event.action

        if (action == KeyEvent.ACTION_DOWN && event.repeatCount > 0) {
            return DebugKeyEventDecision.FORWARD
        }

        when (action) {
            KeyEvent.ACTION_DOWN -> {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) isVolumeUpPressed = true
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) isVolumeDownPressed = true
            }
            KeyEvent.ACTION_UP -> {
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) isVolumeUpPressed = false
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) isVolumeDownPressed = false
                if (!isVolumeUpPressed || !isVolumeDownPressed) isTriggered = false
            }
        }

        if (isVolumeUpPressed && isVolumeDownPressed && !isTriggered) {
            isTriggered = true
            onTriggered()
            return DebugKeyEventDecision.CONSUME
        }
        return DebugKeyEventDecision.FORWARD
    }
}
