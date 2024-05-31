package com.kape.settings.domain

interface IsNumericIpAddressUseCase {
    operator fun invoke(ipAddress: String): Boolean
}