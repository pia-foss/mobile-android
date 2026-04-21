package com.kape.featureflags.di

import com.kape.featureflags.data.FeatureFlagsDataSourceImpl
import com.kape.featureflags.domain.FeatureFlagsDataSource
import com.kape.featureflags.domain.ForceUpdateUseCase
import com.privateinternetaccess.account.AndroidAccountAPI
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class FeatureFlagsModule {
    @Singleton(binds = [FeatureFlagsDataSource::class])
    fun provideFeatureFlagsDataSource(api: AndroidAccountAPI): FeatureFlagsDataSource = FeatureFlagsDataSourceImpl(api)

    @Singleton
    fun provideForceUpdateUseCase(featureFlagsDataSource: FeatureFlagsDataSource): ForceUpdateUseCase =
        ForceUpdateUseCase(featureFlagsDataSource)
}