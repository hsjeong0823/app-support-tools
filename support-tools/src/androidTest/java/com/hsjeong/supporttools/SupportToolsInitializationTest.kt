package com.hsjeong.supporttools

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hsjeong.supporttools.utils.DeepLinkManager
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SupportToolsInitializationTest {
    @Test
    fun provider_initializesSupportToolsWithoutHostCode() {
        assertTrue(SupportTools.isInitializedForTests())
    }

    @Test
    fun provider_doesNotInstallHostSpecificDeepLinks() {
        assertTrue(DeepLinkManager.getDeepLinkList().isEmpty())
    }
}
