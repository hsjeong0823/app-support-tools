package com.hsjeong.supporttools.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ServerTypeParserTest {

    @Test
    fun `unknown stored value falls back to DEV`() {
        assertEquals(ServerType.DEV, UrlConfigManager.parseServerType("BROKEN"))
    }

    @Test
    fun `known stored value is restored`() {
        assertEquals(ServerType.STG, UrlConfigManager.parseServerType("STG"))
    }
}
