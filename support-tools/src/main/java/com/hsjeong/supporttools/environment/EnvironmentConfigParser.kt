package com.hsjeong.supporttools.environment

import com.google.gson.Gson

internal object EnvironmentConfigParser {
    private val gson = Gson()
    private val supportedTargets = setOf("DEV", "STG", "REAL")

    fun parse(json: String): EnvironmentConfigParseResult {
        return try {
            val raw = gson.fromJson(json, RawEnvironmentConfig::class.java)
                ?: return EnvironmentConfigParseResult.Failure("Environment config is empty")
            if (raw.schemaVersion != 1) {
                return EnvironmentConfigParseResult.Failure(
                    "Unsupported schema version: ${raw.schemaVersion}"
                )
            }

            val services = raw.services.orEmpty().mapIndexed { index, service ->
                val id = service.id?.takeIf(String::isNotBlank)
                    ?: return EnvironmentConfigParseResult.Failure(
                        "Service at index $index has a blank id"
                    )
                val sources = service.sourceAuthorities.orEmpty()
                if (sources.isEmpty() || sources.any(String::isBlank)) {
                    return EnvironmentConfigParseResult.Failure(
                        "Service '$id' has no valid source authorities"
                    )
                }
                val targets = service.targets.orEmpty()
                if (targets.keys.any { it !in supportedTargets }) {
                    return EnvironmentConfigParseResult.Failure(
                        "Service '$id' has an unsupported target"
                    )
                }
                EnvironmentServiceConfig(id, sources, targets)
            }

            EnvironmentConfigParseResult.Success(EnvironmentConfig(1, services))
        } catch (t: Throwable) {
            EnvironmentConfigParseResult.Failure(t.message ?: "Failed to parse environment config")
        }
    }

    private data class RawEnvironmentConfig(
        val schemaVersion: Int = 0,
        val services: List<RawEnvironmentServiceConfig>? = null,
    )

    private data class RawEnvironmentServiceConfig(
        val id: String? = null,
        val sourceAuthorities: List<String>? = null,
        val targets: Map<String, String>? = null,
    )
}
