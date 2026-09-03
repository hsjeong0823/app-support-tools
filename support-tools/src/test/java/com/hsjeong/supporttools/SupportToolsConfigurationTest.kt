package com.hsjeong.supporttools

import android.view.KeyEvent
import com.hsjeong.supporttools.startup.DebugKeyEvent
import com.hsjeong.supporttools.startup.DebugKeyEventHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SupportToolsConfigurationTest {
    @Test
    fun existingKeyHandler_observesCurrentSettingWhenShortcutIsDisabled() {
        var enabled = true
        var triggers = 0
        var forwarded = 0
        val handler = DebugKeyEventHandler(
            isEnabled = { enabled },
            onTriggered = { triggers++ },
            forward = {
                forwarded++
                false
            },
        )

        handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP))
        handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN))
        assertEquals(1, triggers)

        enabled = false
        handler.handle(DebugKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_UP))
        handler.handle(DebugKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN))
        handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP))
        handler.handle(DebugKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN))

        assertEquals(1, triggers)
        assertFalse(forwarded == 0)
    }
}
