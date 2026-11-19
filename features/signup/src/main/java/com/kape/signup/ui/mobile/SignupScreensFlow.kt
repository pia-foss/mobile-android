package com.kape.signup.ui.mobile

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.signup.ui.SignupError
import com.kape.signup.ui.SignupStep
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.utils.LocalColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignupScreensFlow() {
    val viewModel: SignupViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.state }.collectAsStateWithLifecycle()
    val connectionState by remember(viewModel) { viewModel.isConnected }.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (state.step) {
        is SignupStep.Consent -> ConsentScreen(false)
        SignupStep.Default -> {
            when (connectionState) {
                true -> {
                    if (state.subscriptionData == null) {
                        viewModel.loadPrices(context as Activity)
                    }
                }

                false -> viewModel.loadEmptyPrices()
            }
            when (state.error) {
                SignupError.EmailInvalid,
                SignupError.RegistrationFailed,
                ->
                    SignupErrorScreen {
                        viewModel.navigateToLogin()
                    }

                null -> {
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
            SignUpScreen()
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