plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url  = uri("https://jitpack.io") }
    }
}

rootProject.name = "hass"