package com.kape.featureflags.di

import com.kape.featureflags.data.FeatureFlagsDataSourceImpl
import com.kape.featureflags.domain.FeatureFlagsDataSource
import com.kape.featureflags.domain.ForceUpdateUseCase
import org.koin.dsl.module

val featureFlagsModule = module {
    single<FeatureFlagsDataSource> { FeatureFlagsDataSourceImpl(get()) }
    single { ForceUpdateUseCase(get()) }
}