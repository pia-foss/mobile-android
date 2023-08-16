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

include(":core")
include(":core:router")
include(":core:payments")
include(":core:utils")
include(":core:vpn:wireguard")
include(":core:vpn:openvpn")
include(":core:vpn:targetprovider")
include(":core:vpn:serviceprovider")
include(":core:vpn:vpnprotocol")
include(":core:vpn:vpnmanager")

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
include(":capabilities:shareevents")
include(":features:regionselection")
include(":capabilities:notifications")
