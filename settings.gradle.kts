pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CGL Hustle"
include(":app")
include(":core:config")
include(":core:network")
include(":core:database")
include(":core:common")
include(":core:sync")
include(":core:ui")
include(":feature:mocktest")
include(":feature:auth")
include(":feature:quiz-config")
include(":feature:active-session")
include(":feature:results")
include(":feature:dashboard")
include(":feature:mcqs")
