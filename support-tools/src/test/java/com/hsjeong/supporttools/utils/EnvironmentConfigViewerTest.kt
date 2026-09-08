package com.hsjeong.supporttools.utils

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnvironmentConfigViewerTest {
    @After
    fun tearDown() {
        UrlConfigManager.clearEnvironmentConfig()
    }

    @Test
    fun `successful load keeps the original json for inspection`() {
        val json = validConfigJson()

        assertTrue(UrlConfigManager.installEnvironmentConfig(json))

        assertEquals(json, UrlConfigManager.getEnvironmentConfigJson())
    }

    @Test
    fun `failed load clears a previously loaded json`() {
        UrlConfigManager.installEnvironmentConfig(validConfigJson())

        UrlConfigManager.installEnvironmentConfig("{")

        assertNull(UrlConfigManager.getEnvironmentConfigJson())
    }

    private fun validConfigJson(): String =
        """
        {
          "schemaVersion": 1,
          "services": [
            {
              "id": "api",
              "sourceAuthorities": ["dev-api.example.test"],
              "targets": {
                "DEV": "dev-api.example.test",
                "STG": "stg-api.example.test",
                "REAL": "api.example.test"
              }
            }
          ]
        }
        """.trimIndent()
}
