package com.kape.shareevents.di

import com.kape.shareevents.data.KpiDataSourceImpl
import com.kape.shareevents.domain.KpiDataSource
import com.kape.shareevents.utils.KpiPrefs
import org.koin.dsl.module

val kpiModule = module {
    single { KpiPrefs(get()) }
    single<KpiDataSource> { KpiDataSourceImpl(get(), get()) }
}