pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "mobile-andorid"
include(":app")
includeBuild("configuration")
include(":core:data")
include(":core:contracts")
include(":core:network")
include(":core:router")
include("core:utils")
include(":features:splash")
include(":features:login")
include(":features:signup")
include(":capabilities:appbar")
include(":features:dedicatedip")
include(":capabilities:ui")
include(":core:regions")
include(":core:account")
include(":core:payments")
include(":core:vpnconnect")
include(":core:obfuscator")
include(":core:portforwarding")
include(":capabilities:featureflags")
include(":core:localprefs:data")
include(":core:localprefs:api")
include(":core:localprefs:prefs")
include(":capabilities:buildconfig")
include(":capabilities:csi")
include(":capabilities:shareevents")
include(":core:vpnlauncher")
include(":capabilities:snooze")
include(":capabilities:networkmanagement")
include(":capabilities:location")
include(":capabilities:rating")
include(":capabilities:sharedui")
include(":features:permissions")
include(":features:inappbrowser")
include(":features:about")
include(":features:tvwelcome")
include(":features:vpnregionselection")
include(":features:obfuscationregionselection")
include(":features:profile")
include(":features:sidemenu")
include(":features:connection")
include(":features:automation")
include(":features:widget")
include(":features:settings")