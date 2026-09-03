package com.hsjeong.supporttools

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hsjeong.supporttools.test.R as TestR
import com.hsjeong.supporttools.utils.PreferencesUtil
import com.hsjeong.supporttools.utils.ServerType
import com.hsjeong.supporttools.utils.UrlConfigManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnvironmentResourceLoadingTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun restoreUrlSwitching() {
        PreferencesUtil.setUrlSwitchingEnable(context, true)
    }

    @Test
    fun validResourceLoadsAndResolvesStoredEnvironment() {
        UrlConfigManager.setServerType(context, ServerType.STG)
        PreferencesUtil.setUrlSwitchingEnable(context, true)

        assertTrue(
            SupportTools.loadEnvironmentConfig(
                context,
                TestR.raw.valid_support_tools_environments,
            )
        )
        assertEquals(
            "stg-api.example.test",
            SupportTools.resolveAuthority(context, "dev-api.example.test"),
        )
    }

    @Test
    fun invalidResourceClearsPreviouslyInstalledConfig() {
        UrlConfigManager.setServerType(context, ServerType.STG)
        PreferencesUtil.setUrlSwitchingEnable(context, true)
        assertTrue(
            SupportTools.loadEnvironmentConfig(
                context,
                TestR.raw.valid_support_tools_environments,
            )
        )

        assertFalse(
            SupportTools.loadEnvironmentConfig(
                context,
                TestR.raw.invalid_support_tools_environments,
            )
        )
        assertEquals(
            "dev-api.example.test",
            SupportTools.resolveAuthority(context, "dev-api.example.test"),
        )
    }

    @Test
    fun unregisteredAuthorityIsUnchanged() {
        PreferencesUtil.setUrlSwitchingEnable(context, true)
        assertTrue(
            SupportTools.loadEnvironmentConfig(
                context,
                TestR.raw.valid_support_tools_environments,
            )
        )

        assertEquals(
            "other.example.test",
            SupportTools.resolveAuthority(context, "other.example.test"),
        )
    }

    @Test
    fun disabledUrlSwitchingLeavesAuthorityUnchanged() {
        UrlConfigManager.setServerType(context, ServerType.STG)
        assertTrue(
            SupportTools.loadEnvironmentConfig(
                context,
                TestR.raw.valid_support_tools_environments,
            )
        )
        PreferencesUtil.setUrlSwitchingEnable(context, false)

        assertEquals(
            "dev-api.example.test",
            SupportTools.resolveAuthority(context, "dev-api.example.test"),
        )
    }
}
