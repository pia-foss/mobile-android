package com.kape.localprefs.data.settings

import com.kape.settings.data.DataEncryption
import com.kape.settings.data.ProtocolSettings
import com.kape.settings.data.Transport
import kotlinx.serialization.Serializable

@Serializable
data class AutomaticSettings(
    override val name: String = "Auto",
    override val auth: String = "Auto",
    override var transport: Transport = Transport.AUTO,
    override var dataEncryption: DataEncryption = DataEncryption.AUTO,
    override var port: String = "Auto",
    override var useSmallPackets: Boolean = false,
    override var handshake: String = "Auto",
    override var mtu: Int = if (useSmallPackets) 1350 else 1420,
) : ProtocolSettings