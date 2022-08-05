package com.kape.signup.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.signup.utils.SignupError
import com.kape.signup.utils.SignupStep
import com.kape.uicomponents.theme.DarkGreen20
import org.koin.androidx.compose.viewModel

@Composable
fun SignupScreensFlow() {

    val viewModel: SignupViewModel by viewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsState()

    when (state.step) {
        SignupStep.Consent -> ConsentScreen(viewModel = viewModel)
        SignupStep.Default -> {
            if (state.loading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DarkGreen20
                    )
                }
            } else {
                viewModel.loadPrices()
            }
            when (state.error) {
                SignupError.EmailInvalid -> TODO()
                SignupError.RegistrationFailed -> TODO()
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
                credentials = (state.step as SignupStep.SignedUp).credentials
            )
        }
        is SignupStep.Subscriptions -> {
            SubscriptionScreen(viewModel = viewModel, (state.step as SignupStep.Subscriptions).data)
        }
    }
}