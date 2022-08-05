package com.kape.share_events.di

import com.kape.share_events.data.KpiDataSourceImpl
import com.kape.share_events.domain.KpiDataSource
import com.kape.share_events.utils.KpiPrefs
import org.koin.dsl.module

val kpiModule = module {
    single { KpiPrefs(get()) }
    single<KpiDataSource> { KpiDataSourceImpl(get(), get()) }
}