package com.hsjeong.supporttools.restart

internal const val HOST_LAUNCHER_ACTIVITY_META_DATA =
    "com.hsjeong.supporttools.HOST_LAUNCHER_ACTIVITY"

internal data class HostLauncher(
    val packageName: String,
    val className: String,
)

internal object HostLauncherSelector {
    fun select(
        candidates: List<HostLauncher>,
        configuredClassName: String?,
        supportToolsComponent: HostLauncher,
    ): HostLauncher? {
        val hostCandidates = candidates.filterNot { it == supportToolsComponent }
        return if (configuredClassName != null) {
            hostCandidates.singleOrNull { it.className == configuredClassName }
        } else {
            hostCandidates.singleOrNull()
        }
    }
}
