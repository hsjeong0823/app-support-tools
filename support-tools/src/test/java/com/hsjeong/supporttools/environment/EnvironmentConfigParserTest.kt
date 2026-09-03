package com.hsjeong.supporttools.environment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnvironmentConfigParserTest {

    @Test
    fun `schema 1 returns success with every service`() {
        val result = EnvironmentConfigParser.parse(
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
                },
                {
                  "id": "web",
                  "sourceAuthorities": ["dev-web.example.test"],
                  "targets": {
                    "DEV": "dev-web.example.test",
                    "STG": "stg-web.example.test",
                    "REAL": "web.example.test"
                  }
                }
              ]
            }
            """.trimIndent()
        )

        assertTrue(result is EnvironmentConfigParseResult.Success)
        assertEquals(2, (result as EnvironmentConfigParseResult.Success).config.services.size)
    }

    @Test
    fun `schema 2 returns failure`() {
        val result = EnvironmentConfigParser.parse("""{"schemaVersion":2,"services":[]}""")

        assertTrue(result is EnvironmentConfigParseResult.Failure)
    }

    @Test
    fun `malformed json returns failure`() {
        val result = EnvironmentConfigParser.parse("{")

        assertTrue(result is EnvironmentConfigParseResult.Failure)
    }

    @Test
    fun `blank service id returns failure`() {
        val result = EnvironmentConfigParser.parse(configJson(id = " "))

        assertTrue(result is EnvironmentConfigParseResult.Failure)
    }

    @Test
    fun `blank source authority returns failure`() {
        val result = EnvironmentConfigParser.parse(configJson(sourceAuthority = " "))

        assertTrue(result is EnvironmentConfigParseResult.Failure)
    }

    private fun configJson(
        id: String = "api",
        sourceAuthority: String = "dev-api.example.test",
    ): String =
        """
        {
          "schemaVersion": 1,
          "services": [
            {
              "id": "$id",
              "sourceAuthorities": ["$sourceAuthority"],
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
