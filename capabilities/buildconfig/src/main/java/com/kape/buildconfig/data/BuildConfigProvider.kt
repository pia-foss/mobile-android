package com.kape.buildconfig.data

class BuildConfigProvider(
    buildFlavor: String,
    buildType: String,
) {

    private val flavor = when (buildFlavor) {
        "google" -> BuildFlavor.GOOGLE
        "amazon" -> BuildFlavor.AMAZON
        "noinapp" -> BuildFlavor.WEB
        "meta" -> BuildFlavor.META
        else -> throw IllegalArgumentException("Unknown app flavor. Please update enum.")
    }

    private val type = when (buildType) {
        "debug" -> BuildType.DEBUG
        "release" -> BuildType.RELEASE
        else -> throw IllegalArgumentException("Unknown app type. Please update enum.")
    }

    fun isGoogleFlavor() =
        flavor == BuildFlavor.GOOGLE

    fun isAmazonFlavor() =
        flavor == BuildFlavor.AMAZON

    fun isWebFlavor() =
        flavor == BuildFlavor.WEB

    fun isMetaFlavor() =
        flavor == BuildFlavor.META

    fun isDebugType() =
        type == BuildType.DEBUG

    fun isReleaseType() =
        type == BuildType.RELEASE
}