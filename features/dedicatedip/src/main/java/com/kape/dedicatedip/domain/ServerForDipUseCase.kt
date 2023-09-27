package com.kape.dedicatedip.domain

import com.kape.regionselection.domain.GetRegionsUseCase
import com.kape.utils.server.Server
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ServerForDipUseCase(private val getRegionsUseCase: GetRegionsUseCase) {

    fun getServerForDip(
        locale: String,
        dip: DedicatedIPInformationResponse.DedicatedIPInformation,
    ): Flow<Server?> =
        flow {
            getRegionsUseCase.loadRegions(locale).collect {
                if (it.isEmpty()) {
                    emit(null)
                    return@collect
                }
                val match =
                    it.filter { server -> server.key == dip.id || server.name.lowercase() == dip.id }
                if (match.isNotEmpty()) {
                    val server = match.first()
                    val updatedEndpointsPerProtocol =
                        mutableMapOf<Server.ServerGroup, List<Server.ServerEndpointDetails>>()
                    server.endpoints.forEach { (serverGroup, serverEndpointDetails) ->
                        val updatedEndpointDetails =
                            mutableListOf<Server.ServerEndpointDetails>()
                        when (serverGroup) {
                            Server.ServerGroup.OPENVPN_TCP,
                            Server.ServerGroup.OPENVPN_UDP,
                            Server.ServerGroup.META,
                            -> {
                                dip.ip?.let { ip ->
                                    dip.cn?.let { cn ->
                                        updatedEndpointDetails.add(
                                            Server.ServerEndpointDetails(
                                                ip,
                                                cn,
                                            ),
                                        )
                                    }
                                }
                            }

                            Server.ServerGroup.WIREGUARD -> {
                                val port: String =
                                    serverEndpointDetails[0].ip.split(":")[1]
                                dip.cn?.let { cn ->
                                    updatedEndpointDetails.add(
                                        Server.ServerEndpointDetails(
                                            dip.ip + ":" + port,
                                            cn,
                                        ),
                                    )
                                }
                            }
                        }
                        updatedEndpointsPerProtocol[serverGroup] = updatedEndpointDetails
                    }

                    emit(
                        Server(
                            server.name,
                            server.iso,
                            null,
                            updatedEndpointsPerProtocol,
                            server.key,
                            server.latitude,
                            server.longitude,
                            server.isGeo,
                            server.isOffline,
                            server.isAllowsPF,
                            dip.dipToken,
                            dip.ip,
                        ),
                    )
                } else {
                    emit(null)
                }
            }
        }
}