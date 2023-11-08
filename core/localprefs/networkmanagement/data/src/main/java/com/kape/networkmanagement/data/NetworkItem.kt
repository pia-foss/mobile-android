package com.kape.networkmanagement.data

import kotlinx.serialization.Serializable

@Serializable
data class NetworkItem(
    val networkName: String,
    val networkType: NetworkType,
    val networkBehavior: NetworkBehavior,
    val isDefaultForMobile: Boolean = false,
    val isDefaultForOpen: Boolean = false,
    val isDefaultForSecure: Boolean = false,
    val isDefault: Boolean = isDefaultForMobile or isDefaultForOpen or isDefaultForSecure,
)

@Serializable
sealed class NetworkBehavior {
    data object AlwaysConnect : NetworkBehavior()
    data object AlwaysDisconnect : NetworkBehavior()
    data object RetainState : NetworkBehavior()
}

@Serializable
sealed class NetworkType {
    data object MobileData : NetworkType()
    data object WifiOpen : NetworkType()
    data object WifiSecure : NetworkType()
    data object WifiCustom : NetworkType()
}