package com.kape.buildconfig.di

import com.kape.buildconfig.data.BuildConfigProvider
import org.koin.dsl.module

fun buildConfigModule(buildFlavor: String, buildType: String) = module {
    single { BuildConfigProvider(buildFlavor, buildType) }
}