package com.hsjeong.supporttools.environment

import com.hsjeong.supporttools.utils.ServerType
import org.junit.Assert.assertEquals
import org.junit.Test

class EnvironmentAuthorityResolverTest {

    @Test
    fun `selected environment returns its target`() {
        val resolver = EnvironmentAuthorityResolver.from(config(service()))

        assertEquals("stg-api.example.test", resolver.resolve("dev-api.example.test", ServerType.STG))
    }

    @Test
    fun `test and dr aliases return the selected target`() {
        val resolver = EnvironmentAuthorityResolver.from(
            config(
                service(
                    sources = listOf(
                        "dev-api.example.test",
                        "test-api.example.test",
                        "dr-api.example.test",
                    )
                )
            )
        )

        assertEquals("api.example.test", resolver.resolve("test-api.example.test", ServerType.REAL))
        assertEquals("api.example.test", resolver.resolve("dr-api.example.test", ServerType.REAL))
    }

    @Test
    fun `unregistered source returns original authority`() {
        val resolver = EnvironmentAuthorityResolver.from(config(service()))

        assertEquals(
            "other.example.test",
            resolver.resolve("other.example.test", ServerType.STG),
        )
    }

    @Test
    fun `blank target returns original authority`() {
        val resolver = EnvironmentAuthorityResolver.from(
            config(service(targets = targets(stg = "")))
        )

        assertEquals(
            "dev-api.example.test",
            resolver.resolve("dev-api.example.test", ServerType.STG),
        )
    }

    @Test
    fun `different targets for the same source return original authority`() {
        val resolver = EnvironmentAuthorityResolver.from(
            config(
                service(id = "api", targets = targets(stg = "stg-api.example.test")),
                service(id = "api-alias", targets = targets(stg = "other-stg-api.example.test")),
            )
        )

        assertEquals(
            "dev-api.example.test",
            resolver.resolve("dev-api.example.test", ServerType.STG),
        )
    }

    private fun config(vararg services: EnvironmentServiceConfig) =
        EnvironmentConfig(schemaVersion = 1, services = services.toList())

    private fun service(
        id: String = "api",
        sources: List<String> = listOf("dev-api.example.test"),
        targets: Map<String, String> = targets(),
    ) = EnvironmentServiceConfig(
        id = id,
        sourceAuthorities = sources,
        targets = targets,
    )

    private fun targets(
        stg: String = "stg-api.example.test",
    ) = mapOf(
        "DEV" to "dev-api.example.test",
        "STG" to stg,
        "REAL" to "api.example.test",
    )
}
