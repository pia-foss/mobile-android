package com.kape.settings.data

interface ProtocolSettings {
    val name: String
    val auth: String
    var transport: Transport
    var dataEncryption: DataEncryption
    var port: String
    var useSmallPackets: Boolean
    var handshake: String
    var mtu: Int
}