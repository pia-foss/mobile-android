package com.kape.dedicatedip.di

import com.kape.dedicatedip.data.DipDataSourceImpl
import com.kape.dedicatedip.domain.DipDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

fun dedicatedIpModule(appModule: Module) = module {
    includes(appModule, localDipModule)
}

val localDipModule = module {
    single<DipDataSource> {DipDataSourceImpl(get())}
}