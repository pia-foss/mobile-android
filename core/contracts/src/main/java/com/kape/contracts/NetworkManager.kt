package com.kape.contracts

interface NetworkManager {
    fun handleCurrentNetwork(ssid: String, isWifi: Boolean)
}