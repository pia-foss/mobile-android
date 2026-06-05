package com.kape.signup.ui.tv

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.SignupError
import com.kape.signup.utils.SignupStep
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvSignupScreensFlow() {
    val viewModel: SignupViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    val connectionState by remember(viewModel) { viewModel.isConnected }.collectAsState()
    val context = LocalContext.current

    when (state.step) {
        SignupStep.Default -> {
            when (connectionState) {
                true -> {
                    if (state.subscriptionData == null) {
                        viewModel.loadPrices(context as Activity)
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