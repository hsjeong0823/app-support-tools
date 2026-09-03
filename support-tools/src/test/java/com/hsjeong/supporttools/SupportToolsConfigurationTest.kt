package com.hsjeong.supporttools

import android.view.KeyEvent
import com.hsjeong.supporttools.startup.DebugKeyEvent
import com.hsjeong.supporttools.startup.DebugKeyEventDecision
import com.hsjeong.supporttools.startup.DebugKeyEventHandler
import org.junit.Assert.assertEquals
import org.junit.Test

class SupportToolsConfigurationTest {
    @Test
    fun keyHandler_forwardsAndResetsStateWhenShortcutIsDisabled() {
        var enabled = true
        val handler = DebugKeyEventHandler(
            isEnabled = { enabled },
        )

        assertEquals(DebugKeyEventDecision.FORWARD, handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP)))

        enabled = false
        assertEquals(
            DebugKeyEventDecision.FORWARD,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN)),
        )

        enabled = true
        assertEquals(
            DebugKeyEventDecision.FORWARD,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN)),
        )
    }

    @Test
    fun keyHandler_forwardsRepeatedKeyDownEvents() {
        val handler = DebugKeyEventHandler(isEnabled = { true })

        assertEquals(
            DebugKeyEventDecision.FORWARD,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP, repeatCount = 1)),
        )
    }

    @Test
    fun keyHandler_forwardsOrdinaryKeyEvents() {
        val handler = DebugKeyEventHandler(isEnabled = { true })

        assertEquals(
            DebugKeyEventDecision.FORWARD,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A)),
        )
    }

    @Test
    fun keyHandler_consumesCompletedVolumeChordOnlyOnce() {
        val handler = DebugKeyEventHandler(isEnabled = { true })

        assertEquals(
            DebugKeyEventDecision.FORWARD,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP)),
        )
        assertEquals(
            DebugKeyEventDecision.CONSUME,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN)),
        )
        assertEquals(
            DebugKeyEventDecision.FORWARD,
            handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN)),
        )
    }
}
