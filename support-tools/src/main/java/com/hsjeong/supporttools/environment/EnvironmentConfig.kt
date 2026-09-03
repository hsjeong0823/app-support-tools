package com.hsjeong.supporttools.environment

internal data class EnvironmentConfig(
    val schemaVersion: Int,
    val services: List<EnvironmentServiceConfig>,
)

internal data class EnvironmentServiceConfig(
    val id: String,
    val sourceAuthorities: List<String>,
    val targets: Map<String, String>,
)

internal sealed interface EnvironmentConfigParseResult {
    data class Success(val config: EnvironmentConfig) : EnvironmentConfigParseResult
    data class Failure(val reason: String) : EnvironmentConfigParseResult
}
