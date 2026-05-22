package com.cglhustle.core.config

/**
 * Single source of truth for all BuildConfig and Environment variable access.
 * Modules should NEVER access BuildConfig directly.
 */
object EnvironmentProvider {

    init {
        validateConfig()
    }

    val primarySupabaseUrl: String
        get() = BuildConfig.CGL_HUSTLE_SUPABASE_URL

    val primarySupabaseAnonKey: String
        get() = BuildConfig.CGL_HUSTLE_SUPABASE_ANON_KEY

    val questionSupabaseUrl: String
        get() = BuildConfig.GK_LLM_SUPABASE_URL

    val questionSupabaseAnonKey: String
        get() = BuildConfig.GK_LLM_SUPABASE_ANON_KEY

    val currentEnvironment: BuildEnvironment
        get() = if (BuildConfig.DEBUG) BuildEnvironment.DEBUG else BuildEnvironment.RELEASE

    /**
     * Validates that all critical infrastructure configurations are present.
     * Crashes early in Debug to prevent silent failures later in the runtime.
     */
    private fun validateConfig() {
        if (BuildConfig.DEBUG) {
            require(BuildConfig.CGL_HUSTLE_SUPABASE_URL.isNotBlank()) {
                "CRITICAL INFRA ERROR: CGL_HUSTLE_SUPABASE_URL is missing."
            }
            require(BuildConfig.CGL_HUSTLE_SUPABASE_ANON_KEY.isNotBlank()) {
                "CRITICAL INFRA ERROR: CGL_HUSTLE_SUPABASE_ANON_KEY is missing."
            }
            require(BuildConfig.GK_LLM_SUPABASE_URL.isNotBlank()) {
                "CRITICAL INFRA ERROR: GK_LLM_SUPABASE_URL is missing. GK LLM Backend must be configured."
            }
            require(BuildConfig.GK_LLM_SUPABASE_ANON_KEY.isNotBlank()) {
                "CRITICAL INFRA ERROR: GK_LLM_SUPABASE_ANON_KEY is missing. GK LLM Backend must be configured."
            }
        }
    }
}
