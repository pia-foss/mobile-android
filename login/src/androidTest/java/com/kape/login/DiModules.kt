package com.kape.login

import com.kape.login.domain.LoginUseCase
import com.kape.login.provider.AccountModuleStateProvider
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.utils.LoginState
import com.kape.login.utils.Prefs
import com.privateinternetaccess.account.AndroidAccountAPI
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val useCase = mockk<LoginUseCase>(relaxed = true) {
    coEvery { login(any(), any()) } returns flow {
        emit(LoginState.Failed)
    }
}

val viewModel: LoginViewModel = mockk(relaxed = true) {

    every { loginState } returns MutableStateFlow(getScreenState(LoginState.Successful))
}

val testLoginModule = module {
    single { mockk<Prefs>(relaxed = true) }
    single { mockk<AccountModuleStateProvider>(relaxed = true) }
    single { mockk<AndroidAccountAPI>(relaxed = true) }
    single { mockk<LoginRepository>(relaxed = true) }
    single { useCase }
    viewModel { viewModel }
}