package com.kape.contracts

import com.kape.data.vpnserver.VpnServer
import com.kape.settings.data.CustomDns
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.vpnmanager.data.models.ClientConfiguration

interface ConnectionConfigurationUseCase {
    suspend fun generateConnectionConfiguration(server: VpnServer): ClientConfiguration

    suspend fun updateServerConfig(
        server: VpnServer,
        protocol: VpnProtocols,
        transport: Transport,
        dataEncryption: DataEncryption,
        selectedDnsOptions: DnsOptions,
        port: String,
        maceEnabled: Boolean,
        customDns: CustomDns,
    ): Boolean
}