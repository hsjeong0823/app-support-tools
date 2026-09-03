package com.hsjeong.supporttools.utils

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class DeepLinkManagerTest {
    @After
    fun tearDown() {
        DeepLinkManager.setDeepLinkList(emptyList())
    }

    @Test
    fun setDeepLinkList_retainsExactlyTheCallerProvidedEntries() {
        val callerProvidedEntries = listOf(
            DeepLinkData("Account", "sample://account"),
            DeepLinkData("Orders", "https://example.com/orders"),
        )

        DeepLinkManager.setDeepLinkList(callerProvidedEntries)

        assertEquals(callerProvidedEntries, DeepLinkManager.getDeepLinkList())
    }
}
