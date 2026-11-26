package com.kape.profile.ui.vm

import androidx.lifecycle.ViewModel
import com.kape.contracts.Router
import com.kape.data.LoginWithCredentials
import com.kape.data.Subscribe
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AccountDeletedViewModel(
    private val router: Router,
) : ViewModel() {
    fun navigateToLogin() = router.updateDestination(LoginWithCredentials)

    fun navigateToSubscribe() = router.updateDestination(Subscribe)
}