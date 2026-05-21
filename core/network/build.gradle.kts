import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.cglhustle.core.network"
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

    // Try to get values from Env variables (GitHub Actions) first, then local.properties, then fallback to empty string
    val supabaseUrlEnv = System.getenv("SUPABASE_URL")
    val supabaseUrl = if (!supabaseUrlEnv.isNullOrEmpty()) supabaseUrlEnv else localProperties.getProperty("SUPABASE_URL", "")

    val supabaseKeyEnv = System.getenv("SUPABASE_ANON_KEY")
    val supabaseAnonKey = if (!supabaseKeyEnv.isNullOrEmpty()) supabaseKeyEnv else localProperties.getProperty("SUPABASE_ANON_KEY", "")

    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint {
        abortOnError = true
        warningsAsErrors = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:database")) // added this for SyncEventDao

    implementation(libs.androidx.core.ktx)
    implementation(project(":core:common"))

    // WorkManager
    implementation(libs.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.gotrue)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.kotlinx.serialization.json)
}
