package com.kape.connection.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AwgObfuscationSettings(
    @SerialName("junkPacketCount")
    val junkPacketCount: Long,
    @SerialName("junkPacketMinSize")
    val junkPacketMinSize: Long,
    @SerialName("junkPacketMaxSize")
    val junkPacketMaxSize: Long,
    @SerialName("initPacketJunkSize")
    val initPacketJunkSize: Long,
    @SerialName("responsePacketJunkSize")
    val responsePacketJunkSize: Long,
    @SerialName("initPacketMagicHeader")
    val initPacketMagicHeader: Long,
    @SerialName("responsePacketMagicHeader")
    val responsePacketMagicHeader: Long,
    @SerialName("underloadPacketMagicHeader")
    val underloadPacketMagicHeader: Long,
    @SerialName("transportPacketMagicHeader")
    val transportPacketMagicHeader: Long,
)