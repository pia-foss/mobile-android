package com.kape.vpnconnect.domain

interface GetActiveInterfaceDnsUseCase {
    operator fun invoke(): List<String>
}