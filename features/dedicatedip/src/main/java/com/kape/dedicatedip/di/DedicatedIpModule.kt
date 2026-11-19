package com.kape.dedicatedip.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [DipStoreModule::class])
@ComponentScan("com.kape.dedicatedip")
class DedicatedIpModule