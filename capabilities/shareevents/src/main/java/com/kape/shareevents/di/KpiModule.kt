package com.kape.shareevents.di

import com.kape.shareevents.KpiPrefs
import com.kape.shareevents.data.KpiDataSourceImpl
import com.kape.shareevents.domain.KpiDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

fun kpiModule(appModule: Module) = module {
    includes(appModule, localKpiModule)
}

private val localKpiModule = module {
    single { KpiPrefs(get()) }
    single<KpiDataSource> { KpiDataSourceImpl(get(), get(), get()) }
}