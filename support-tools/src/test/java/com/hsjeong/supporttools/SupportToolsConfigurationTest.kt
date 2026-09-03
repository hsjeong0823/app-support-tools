package com.hsjeong.supporttools

import com.hsjeong.supporttools.config.AppSupportConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportToolsConfigurationTest {
    @Test
    fun volumeShortcut_isEvaluatedFromCurrentDebugConfiguration() {
        val enabled = AppSupportConfig.Builder().enableVolumeShortcut(true).build()
        val disabled = AppSupportConfig.Builder().enableVolumeShortcut(false).build()

        assertTrue(SupportTools.isVolumeShortcutEnabledForTests(true, enabled))
        assertFalse(SupportTools.isVolumeShortcutEnabledForTests(true, disabled))
        assertFalse(SupportTools.isVolumeShortcutEnabledForTests(false, enabled))
    }
}
