package com.kape.signup.ui.tv

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.signup.ui.SignupError
import com.kape.signup.ui.SignupStep
import com.kape.signup.ui.vm.SignupViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TvSignupScreensFlow() {
    val viewModel: SignupViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsStateWithLifecycle()
    val connectionState by remember(viewModel) { viewModel.isConnected }.collectAsStateWithLifecycle()
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
            TvConsentScreen(false)
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
            if (state.subscriptionData == null) {
                TvNoPlansScreen()
            } else {
                TvSignUpScreen()
            }
        }
    }
}