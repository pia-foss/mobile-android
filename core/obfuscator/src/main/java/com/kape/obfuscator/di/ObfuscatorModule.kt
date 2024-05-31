package com.kape.obfuscator.di

import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StartObfuscatorProcessEventHandler
import com.kape.obfuscator.domain.StopObfuscatorProcess
import org.koin.core.module.Module
import org.koin.dsl.module

fun obfuscatorModule(appModule: Module) = module {
    includes(appModule, localObfuscatorModule)
}

val localObfuscatorModule = module {
    single { StartObfuscatorProcessEventHandler() }
    single { StartObfuscatorProcess(get(), get()) }
    single { StopObfuscatorProcess(get()) }
}