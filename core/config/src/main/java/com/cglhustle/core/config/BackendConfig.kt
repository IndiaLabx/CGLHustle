package com.cglhustle.core.config

/**
 * Exposes Dual Backend Configuration for application services and DI modules.
 */
object BackendConfig {

    val primaryBackendUrl: String
        get() = EnvironmentProvider.primarySupabaseUrl

    val primaryAnonKey: String
        get() = EnvironmentProvider.primarySupabaseAnonKey

    val questionBackendUrl: String
        get() = EnvironmentProvider.questionSupabaseUrl

    val questionAnonKey: String
        get() = EnvironmentProvider.questionSupabaseAnonKey

}
