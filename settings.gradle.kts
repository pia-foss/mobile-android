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
            url = uri("https://maven.pkg.github.com/pia-foss/mobile-android-vpn-manager")
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
        maven {
            url = uri("https://maven.pkg.github.com/pia-foss/mobile-shared-account")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/pia-foss/mobile-shared-csi")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/pia-foss/mobile-shared-kpi")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "PIA"
include(":app")
includeBuild("configuration")

include(":capabilities")
include(":capabilities:ui")
include(":capabilities:shareevents")
include(":capabilities:notifications")
include(":capabilities:csi")
include(":capabilities:location")
include(":capabilities:networkmanagement")
include(":capabilities:snooze")
include(":capabilities:buildconfig")

include(":core")
include(":core:router")
include(":core:payments")
include(":core:utils")
include(":core:vpnconnect")
include(":core:localprefs")
include(":core:portforwarding")
include(":core:httpclient")
include(":core:regions")
include(":core:obfuscator")
include(":core:vpnlauncher")

include(":features")
include(":features:splash")
include(":features:appbar")
include(":features:signup")
include(":features:login")
include(":features:connection")
include(":features:settings")
include(":features:sidemenu")
include(":features:profile")
include(":features:vpnregionselection")
include(":features:obfuscationregionselection")
include(":features:dedicatedip")
include(":features:permissions")
include(":features:automation")
include(":features:widget")
include(":features:about")
include(":features:customization")
include(":features:inappbrowser")
include(":features:tvwelcome")
include(":features:rating")
include(":capabilities:featureflags")
include(":core:localprefs:prefs")
include(":core:localprefs:data")
include(":core:contracts")
include(":core:data")
