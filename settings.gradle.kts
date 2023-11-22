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
        mavenLocal()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/xvpn/kp_android_vpn_manager")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/pia-foss/mobile-shared-regions")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "PIA"
include(":app")

include(":capabilities")
include(":capabilities:ui")
include(":capabilities:shareevents")
include(":capabilities:notifications")
include(":capabilities:csi")

include(":core")
include(":core:router")
include(":core:payments")
include(":core:utils")
include(":core:vpnconnect")
include(":core:localprefs")
include(":core:localprefs:connection")
include(":core:localprefs:settings")
include(":core:localprefs:signup")
include(":core:localprefs:shareevents")
include(":core:localprefs:payments")
include(":core:localprefs:regions")
include(":core:localprefs:settings:data")
include(":core:localprefs:payments:data")
include(":core:portforwarding")
include(":core:httpclient")
include(":core:localprefs:dip")
include(":core:localprefs:csi")
include(":core:regions")

include(":features")
include(":features:splash")
include(":features:appbar")
include(":features:signup")
include(":features:login")
include(":features:connection")
include(":features:settings")
include(":features:sidemenu")
include(":features:profile")
include(":features:regionselection")
include(":features:dedicatedip")
include(":features:permissions")
include(":features:automation")
include(":capabilities:location")
include(":capabilities:networkmanagement")
include(":core:localprefs:networkmanagement")
include(":core:localprefs:networkmanagement:data")
include(":features:widget")
