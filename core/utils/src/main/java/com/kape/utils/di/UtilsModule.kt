package com.kape.utils.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [CoroutinesModule::class])
@ComponentScan("com.kape.utils")
class UtilsModule