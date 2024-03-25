package com.kape.signup.ui.tv

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.SignupStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvSignupScreensFlow() {
    val viewModel: SignupViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    val connectionState by remember(viewModel) { viewModel.isConnected }.collectAsState()
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

    when (state.step) {
        SignupStep.Consent,
        SignupStep.Default,
        is SignupStep.SignedUp,
        -> {
            // TBD
        }
        SignupStep.Email -> {
            TvEmailScreen()
        }
        SignupStep.LoadingPlans,
        SignupStep.InProcess,
        -> {
            TvLoadingScreen()
        }
        SignupStep.Subscriptions -> {
            TvSignUpScreen()
        }
    }
}