package com.hsjeong.supporttools.restart

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HostLauncherSelectorTest {
    private val supportTools = component("com.hsjeong.supporttools.ui.main.SupportToolsActivity")
    private val main = component("com.example.host.MainActivity")
    private val alternate = component("com.example.host.AlternateActivity")

    @Test
    fun `selects the only host launcher`() {
        assertEquals(
            main,
            HostLauncherSelector.select(listOf(supportTools, main), null, supportTools),
        )
    }

    @Test
    fun `never selects the SupportTools launcher`() {
        assertNull(HostLauncherSelector.select(listOf(supportTools), null, supportTools))
    }

    @Test
    fun `selects configured metadata launcher from multiple hosts`() {
        assertEquals(
            alternate,
            HostLauncherSelector.select(
                listOf(supportTools, main, alternate),
                "com.example.host.AlternateActivity",
                supportTools,
            ),
        )
    }

    @Test
    fun `returns null when metadata does not match a host launcher`() {
        assertNull(
            HostLauncherSelector.select(
                listOf(supportTools, main, alternate),
                "com.example.host.MissingActivity",
                supportTools,
            )
        )
    }

    @Test
    fun `returns null for multiple hosts without metadata`() {
        assertNull(
            HostLauncherSelector.select(
                listOf(supportTools, main, alternate),
                null,
                supportTools,
            )
        )
    }

    private fun component(className: String) = HostLauncher("com.example.host", className)
}
