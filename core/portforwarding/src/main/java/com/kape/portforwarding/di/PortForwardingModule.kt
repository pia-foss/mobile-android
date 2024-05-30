package com.kape.portforwarding.di

import com.kape.httpclient.data.CertificatePinningClientImpl
import com.kape.httpclient.domain.CertificatePinningClient
import com.kape.portforwarding.data.PortForwardingApiImpl
import com.kape.portforwarding.domain.PortForwardingApi
import com.kape.portforwarding.domain.PortForwardingUseCase
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun portForwardingModule(appModule: Module) = module {
    includes(appModule, localPortForwardingModule)
}

private val localPortForwardingModule = module {
    single<CertificatePinningClient> {
        CertificatePinningClientImpl(get(named("certificate")))
    }
    single<PortForwardingApi> { PortForwardingApiImpl(get(), get(named("user-agent"))) }
    single { PortForwardingUseCase(get(), get(), get()) }
}