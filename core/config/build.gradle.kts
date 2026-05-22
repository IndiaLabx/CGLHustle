import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.cglhustle.core.config"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    // Load properties from local.properties if it exists (for local development)
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    val primaryUrlEnv = System.getenv("CGL_HUSTLE_SUPABASE_URL")
    val primaryUrl = if (!primaryUrlEnv.isNullOrEmpty()) primaryUrlEnv else localProperties.getProperty("CGL_HUSTLE_SUPABASE_URL", "")

    val primaryKeyEnv = System.getenv("CGL_HUSTLE_SUPABASE_ANON_KEY")
    val primaryKey = if (!primaryKeyEnv.isNullOrEmpty()) primaryKeyEnv else localProperties.getProperty("CGL_HUSTLE_SUPABASE_ANON_KEY", "")

    val gkLlmUrlEnv = System.getenv("GK_LLM_SUPABASE_URL")
    val gkLlmUrl = if (!gkLlmUrlEnv.isNullOrEmpty()) gkLlmUrlEnv else localProperties.getProperty("GK_LLM_SUPABASE_URL", "")

    val gkLlmKeyEnv = System.getenv("GK_LLM_SUPABASE_ANON_KEY")
    val gkLlmKey = if (!gkLlmKeyEnv.isNullOrEmpty()) gkLlmKeyEnv else localProperties.getProperty("GK_LLM_SUPABASE_ANON_KEY", "")

    defaultConfig {
        minSdk = 26

        buildConfigField("String", "CGL_HUSTLE_SUPABASE_URL", "\"$primaryUrl\"")
        buildConfigField("String", "CGL_HUSTLE_SUPABASE_ANON_KEY", "\"$primaryKey\"")
        buildConfigField("String", "GK_LLM_SUPABASE_URL", "\"$gkLlmUrl\"")
        buildConfigField("String", "GK_LLM_SUPABASE_ANON_KEY", "\"$gkLlmKey\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // Avoid javax inject for now, usually it comes transitively. Let's see if we need it.
}
