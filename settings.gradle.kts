pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "PIA"
include(":app")

include(":capabilities")
include(":capabilities:ui")
include(":capabilities:shareevents")
include(":capabilities:notifications")

include(":core")
include(":core:router")
include(":core:payments")
include(":core:utils")

include(":features:splash")
include(":features")
include(":features:appbar")
include(":features:signup")
include(":features:login")
include(":features:connection")
include(":features:settings")
include(":features:sidemenu")
include(":features:profile")
include(":features:vpnpermission")
include(":features:regionselection")

include(":core:vpnconnect")
