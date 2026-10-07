package com.kape.contracts

interface NetworkManager {
    /**
     * @param isNetworkChange true when the device moved to a different network while the app was running,
     * false when the current network is (re)delivered, e.g. on process start or after the rules changed.
     */
    fun handleCurrentNetwork(
        ssid: String,
        isWifi: Boolean,
        isNetworkChange: Boolean,
    )
}