package com.kape.csi.di

import com.kape.csi.data.CsiDataSourceImpl
import com.kape.csi.domain.CsiDataSource
import com.kape.csi.domain.SendLogUseCase
import com.privateinternetaccess.csi.CSIAPI
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class CsiModule {
    @Singleton(binds = [CsiDataSource::class])
    fun provideCsiDataSource(api: CSIAPI): CsiDataSource = CsiDataSourceImpl(api)

    @Singleton
    fun provideSendLogUseCase(dataSource: CsiDataSource): SendLogUseCase = SendLogUseCase(dataSource)
}