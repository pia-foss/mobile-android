package com.kape.connection.domain

import android.app.Notification
import android.app.PendingIntent
import com.kape.connection.utils.TempSettings
import com.privateinternetaccess.kapevpnmanager.models.ClientConfiguration
import com.privateinternetaccess.kapevpnmanager.models.OpenVpnClientConfiguration
import com.privateinternetaccess.kapevpnmanager.models.ServerList
import com.privateinternetaccess.kapevpnmanager.models.WireguardClientConfiguration
import com.privateinternetaccess.kapevpnmanager.presenters.VPNManagerConnectionListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConnectionUseCase(private val connectionSource: ConnectionDataSource) : KoinComponent {

    private val certificate: String by inject()

    suspend fun startConnection(
        settings: TempSettings,
        configureIntent: PendingIntent,
        notification: Notification,
        listener: VPNManagerConnectionListener
    ): Flow<Boolean> = flow {
        val index = connectionSource.getVpnToken().indexOf(":")
        val clientConfiguration = ClientConfiguration(
            sessionName = settings.sessionName,
            configureIntent = configureIntent,
            protocolTarget = settings.protocol,
            mtu = settings.mtu,
            port = settings.port,
            dnsList = listOf(settings.dns),
            notificationId = 123,
            notification = notification,
            allowedApplicationPackages = emptyList(),
            disallowedApplicationPackages = emptyList(),
            allowLocalNetworkAccess = settings.allowLocalNetworkAccess,
            serverList = ServerList(
                servers = listOf(
                    ServerList.Server(
                        ip = settings.server,
                        commonName = settings.serverCommonName,
                        latency = 10
                    ),
                )
            ),
            openVpnClientConfiguration = OpenVpnClientConfiguration(
                caCertificate = certificate,
                cipher = settings.openVpnCipher,
                transport = settings.openVpnTransport,
                username = connectionSource.getVpnToken().substring(0, index),
                password = connectionSource.getVpnToken().substring(index + 1)
            ),
            wireguardClientConfiguration = WireguardClientConfiguration(
                token = connectionSource.getVpnToken(),
                pinningCertificate = certificate
            )
        )

        connectionSource.startConnection(clientConfiguration, listener).collect {
            emit(it)
        }
    }

    suspend fun stopConnection(): Flow<Boolean> = flow {
        connectionSource.stopConnection().collect {
            emit(it)
        }
    }

}