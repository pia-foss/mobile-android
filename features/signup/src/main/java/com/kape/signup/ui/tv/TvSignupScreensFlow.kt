package com.kape.signup.ui.tv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.router.LocalNavigator
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.SignupError
import com.kape.signup.utils.SignupStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvSignupScreensFlow() {
    val viewModel: SignupViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    val connectionState by remember(viewModel) { viewModel.isConnected }.collectAsState()
    val destination by viewModel.router.getNavigationState().collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current

    destination?.let {
        navigator.navigateTo(it)
        viewModel.router.resetNavigation()
    }

    when (state.step) {
        SignupStep.Default -> {
            when (connectionState) {
                true -> {
                    if (viewModel.subscriptionData.value == null) {
                        viewModel.loadPrices()
                    }
                }
                false -> {
                    viewModel.loadEmptyPrices()
                }
            }
            state.error?.let {
                when (it) {
                    SignupError.EmailInvalid,
                    SignupError.RegistrationFailed,
                    -> {
                        TvSignupErrorScreen()
                    }
                }
            }
        }
        is SignupStep.SignedUp -> {
            TvCredentialsScreen(credentials = (state.step as SignupStep.SignedUp).credentials)
        }
        SignupStep.Consent -> {
            TvConsentScreen()
        }
        SignupStep.Email -> {
            TvEmailScreen()
        }
        SignupStep.LoadingPlans,
        SignupStep.InProcess,
        -> {
            TvLoadingScreen()
        }
        is SignupStep.Subscriptions -> {
            TvSignUpScreen()
        }
    }
}