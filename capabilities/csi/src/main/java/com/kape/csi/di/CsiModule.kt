package com.kape.csi.di

import com.kape.csi.data.CsiDataSourceImpl
import com.kape.csi.domain.CsiDataSource
import com.kape.csi.domain.SendLogUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

fun csiModule(appModule: Module) = module {
    includes(appModule, localCsiModule)
}

private val localCsiModule = module {
    single<CsiDataSource> { CsiDataSourceImpl(get()) }
    single { SendLogUseCase(get()) }
}