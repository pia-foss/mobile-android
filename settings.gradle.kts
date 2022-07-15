pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PIA"
include(":app")
include(":login")
include(":account")
include(":core")
include(":profile")
include(":vpn_permissions")
include(":uicomponents")
include(":regions")
include(":region_selection")
include(":sidemenu")
include(":router")
include(":splash")
include(":connection")
include(":payments")
