package com.hsjeong.supporttools

import android.view.KeyEvent
import com.hsjeong.supporttools.startup.DebugKeyEvent
import com.hsjeong.supporttools.startup.DebugKeyEventDecision
import com.hsjeong.supporttools.startup.DebugKeyEventHandler
import org.junit.Assert.assertEquals
import org.junit.Test

class SupportToolsConfigurationTest {
    @Test
    fun existingKeyHandler_observesCurrentSettingWhenShortcutIsDisabled() {
        var enabled = true
        var triggers = 0
        val handler = DebugKeyEventHandler(
            isEnabled = { enabled },
            onTriggered = { triggers++ },
        )

        assertEquals(DebugKeyEventDecision.FORWARD, handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP)))
        assertEquals(
            DebugKeyEventDecision.CONSUME,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN)),
        )
        assertEquals(1, triggers)

        assertEquals(
            DebugKeyEventDecision.FORWARD,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP, repeatCount = 1)),
        )
        assertEquals(
            DebugKeyEventDecision.FORWARD,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A)),
        )

        enabled = false
        assertEquals(
            DebugKeyEventDecision.FORWARD,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN)),
        )

        assertEquals(1, triggers)
    }
}
