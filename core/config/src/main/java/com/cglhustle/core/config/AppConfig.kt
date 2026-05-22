package com.cglhustle.core.config

/**
 * Centralized application-wide configuration access.
 */
object AppConfig {

    val isDebug: Boolean
        get() = EnvironmentProvider.currentEnvironment == BuildEnvironment.DEBUG

    val isRelease: Boolean
        get() = EnvironmentProvider.currentEnvironment == BuildEnvironment.RELEASE

    // Placeholders for future app-wide configs like VersionName, FeatureFlags, etc.
}
