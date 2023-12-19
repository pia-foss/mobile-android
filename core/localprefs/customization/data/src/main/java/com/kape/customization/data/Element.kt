package com.kape.customization.data

import kotlinx.serialization.Serializable

@Serializable
sealed class Element {
    @Serializable
    data object RegionSelection : Element()

    @Serializable
    data object ConnectionInfo : Element()

    @Serializable
    data object QuickConnect : Element()

    @Serializable
    data object QuickSettings : Element()

    @Serializable
    data object Snooze : Element()

    @Serializable
    data object IpInfo : Element()

    @Serializable
    data object Traffic : Element()
}