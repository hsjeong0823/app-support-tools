package com.hsjeong.supporttools.environment

import com.hsjeong.supporttools.utils.ServerType

internal class EnvironmentAuthorityResolver private constructor(
    private val sourceToServices: Map<String, List<EnvironmentServiceConfig>>,
) {
    fun resolve(originalAuthority: String, serverType: ServerType): String {
        val services = sourceToServices[originalAuthority] ?: return originalAuthority
        val targets = services.map { service ->
            service.targets[serverType.name]?.takeIf(String::isNotBlank)
                ?: return originalAuthority
        }.distinct()
        return targets.singleOrNull() ?: originalAuthority
    }

    companion object {
        fun from(config: EnvironmentConfig): EnvironmentAuthorityResolver {
            val sourceToServices = config.services
                .flatMap { service ->
                    service.sourceAuthorities.map { source -> source to service }
                }
                .groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second },
                )
                .mapValues { (_, services) -> services.toList() }
                .toMap()
            return EnvironmentAuthorityResolver(sourceToServices)
        }
    }
}
