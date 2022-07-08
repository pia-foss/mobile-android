package com.kape.region_selection.di

import com.kape.region_selection.data.RegionDataSourceImpl
import com.kape.region_selection.domain.RegionDataSource
import org.koin.dsl.module

val regionModule = module {
    single<RegionDataSource> { RegionDataSourceImpl() }
}