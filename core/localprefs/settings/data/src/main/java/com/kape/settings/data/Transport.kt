package com.kape.settings.data

enum class Transport(val value: String) {
    UDP("UDP"),
    TCP("TCP"), ;

    companion object {
        fun fromName(name: String) = Transport.values().find { it.value == name }
    }
}