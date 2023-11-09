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
    @Serializable
    data object AlwaysConnect : NetworkBehavior()

    @Serializable
    data object AlwaysDisconnect : NetworkBehavior()

    @Serializable
    data object RetainState : NetworkBehavior()
}

@Serializable
sealed class NetworkType {
    @Serializable
    data object MobileData : NetworkType()

    @Serializable
    data object WifiOpen : NetworkType()

    @Serializable
    data object WifiSecure : NetworkType()

    @Serializable
    data object WifiCustom : NetworkType()
}