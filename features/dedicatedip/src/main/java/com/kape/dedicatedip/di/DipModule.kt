package com.kape.dedicatedip.di

import com.kape.dedicatedip.data.DipDataSourceImpl
import com.kape.dedicatedip.domain.ActivateDipUseCase
import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dedicatedip.domain.ServerForDipUseCase
import com.kape.dip.DipPrefs
import org.koin.core.module.Module
import org.koin.dsl.module

fun dedicatedIpModule(appModule: Module) = module {
    includes(appModule, localDipModule)
}

val localDipModule = module {
    single { DipPrefs(get()) }
    single<DipDataSource> { DipDataSourceImpl(get(), get()) }
    single { ServerForDipUseCase(get()) }
    single { ActivateDipUseCase(get()) }
}