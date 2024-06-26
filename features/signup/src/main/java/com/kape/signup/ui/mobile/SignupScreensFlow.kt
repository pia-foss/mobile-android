package com.kape.signup.ui.mobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.SignupError
import com.kape.signup.utils.SignupStep
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupScreensFlow() {
    val viewModel: SignupViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()
    val connectionState by remember(viewModel) { viewModel.isConnected }.collectAsState()

    when (state.step) {
        SignupStep.Consent -> ConsentScreen(viewModel = viewModel)
        SignupStep.Default -> {
            when (connectionState) {
                true -> {
                    if (viewModel.subscriptionData.value == null) {
                        viewModel.loadPrices()
                    }
                }

                false -> viewModel.loadEmptyPrices()
            }
            when (state.error) {
                SignupError.EmailInvalid -> TODO()
                SignupError.RegistrationFailed -> SignupErrorScreen {
                    viewModel.navigateToLogin()
                }

                null -> {
                    // no-op
                }
            }
        }

        SignupStep.Email -> EmailScreen(viewModel = viewModel)
        SignupStep.InProcess -> LoadingScreen()
        is SignupStep.SignedUp -> {
            CredentialsScreen(
                viewModel = viewModel,
                credentials = (state.step as SignupStep.SignedUp).credentials,
            )
        }

        is SignupStep.Subscriptions -> {
            when (connectionState) {
                true -> {
                    if (viewModel.subscriptionData.value == null) {
                        viewModel.loadPrices()
                    }
                }

                false -> viewModel.loadEmptyPrices()
            }
            SignUpScreen(viewModel = viewModel, viewModel.subscriptionData.value)
        }

        SignupStep.LoadingPlans -> {
            if (state.loading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = LocalColors.current.primary,
                    )
                }
            }
        }
    }
}