package com.hsjeong.supporttools.config

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSupportConfigTest {
    @Test
    fun volumeShortcut_isDisabledByDefault() {
        assertFalse(AppSupportConfig.Builder().build().enableVolumeShortcut)
    }

    @Test
    fun volumeShortcut_canBeEnabledExplicitly() {
        val config = AppSupportConfig.Builder()
            .enableVolumeShortcut(true)
            .build()

        assertTrue(config.enableVolumeShortcut)
    }
}
