package com.kape.dedicatedip.di

import com.kape.dedicatedip.data.DipDataSourceImpl
import com.kape.dedicatedip.domain.ActivateDipUseCase
import com.kape.dedicatedip.domain.DipDataSource
import com.kape.dedicatedip.domain.RenewDipUseCase
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.dip.DipPrefs
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun dedicatedIpModule(appModule: Module) = module {
    includes(appModule, localDipModule)
}

val localDipModule = module {
    single { DipPrefs(get()) }
    single<DipDataSource> { DipDataSourceImpl(get(), get()) }
    single { ActivateDipUseCase(get()) }
    single { RenewDipUseCase(get()) }
    viewModel { DipViewModel(get(), get(), get(), get()) }
}