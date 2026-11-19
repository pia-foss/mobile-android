package com.kape.location.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [FusedLocationModule::class])
@ComponentScan("com.kape.location")
class LocationModule