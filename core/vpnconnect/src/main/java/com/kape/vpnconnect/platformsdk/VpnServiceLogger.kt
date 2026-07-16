package com.kape.platformsdk.vpn.service

interface VpnServiceLogger {
    fun trace(message: String)

    fun debug(message: String)

    fun info(message: String)

    fun warning(message: String)

    fun error(message: String)
}