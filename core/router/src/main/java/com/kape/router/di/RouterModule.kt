package com.kape.router.di

import com.kape.contracts.Router
import com.kape.router.RouterImpl
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
class RouterModule {

    @Singleton(binds = [Router::class])
    fun provideRouter(): Router = RouterImpl()
}