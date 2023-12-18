package com.kape.customization.data

import kotlinx.serialization.Serializable

@Serializable
sealed class Element {
    data object RegionSelection : Element()
    data object ConnectionInfo : Element()
    data object QuickConnect : Element()
    data object QuickSettings : Element()
    data object Snooze : Element()
    data object IpInfo : Element()
    data object Traffic : Element()
}