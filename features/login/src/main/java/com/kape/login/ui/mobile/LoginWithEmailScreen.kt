package com.kape.login.ui.mobile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kape.login.ui.vm.mobile.LoginWithEmailViewModel
import com.kape.login.utils.LoginError
import com.kape.login.utils.LoginScreenState
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.ErrorCard
import com.kape.ui.mobile.elements.NoNetworkBanner
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.Input
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginWithEmailScreen() =
    Screen {
        val viewModel: LoginWithEmailViewModel = koinViewModel()

        val state by remember(viewModel) { viewModel.state }.collectAsState()
        val isConnected by remember(viewModel) { viewModel.isConnected }.collectAsState()
        val currentContext = LocalContext.current
        val noNetworkMessage = stringResource(id = com.kape.ui.R.string.no_internet)
        val email = rememberSaveable { mutableStateOf("") }

        val onSendLinkClick: () -> Unit = {
            if (isConnected) {
                viewModel.loginWithEmail(email.value)
            } else {
                Toast.makeText(currentContext, noNetworkMessage, Toast.LENGTH_SHORT).show()
            }
        }

        AdaptiveSplitContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
            header = { isCompact ->
                // Above everything in portrait; beside the form when wide
                if (isCompact && !isConnected) {
                    NoNetworkBanner(noNetworkMessage = noNetworkMessage)
                }
                LoginWithEmailHeader()
            },
            body = { isCompact ->
                if (!isCompact) {
                    if (!isConnected) {
                        NoNetworkBanner(noNetworkMessage = noNetworkMessage)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                LoginWithEmailForm(email = email, state = state, onSendLinkClick = onSendLinkClick)
            },
        )

        if (state.flowCompleted) {
            val message = stringResource(id = com.kape.ui.R.string.login_with_magic_link_success)
            LaunchedEffect(
                key1 = state,
                block = {
                    Toast.makeText(currentContext, message, Toast.LENGTH_SHORT).show()
                    viewModel.navigateToLoginWithCredentials()
                },
            )
        }
    }

@Composable
private fun LoginWithEmailHeader() {
    Image(
        painter = painterResource(id = com.kape.ui.R.drawable.ic_logo_large),
        contentDescription = "logo",
        // A share of the width rather than fixed side padding, which left no room in narrow columns
        modifier =
            Modifier
                .padding(top = 36.dp, bottom = 24.dp)
                .fillMaxWidth(LOGO_WIDTH_FRACTION),
    )
    Text(
        text = stringResource(id = com.kape.ui.R.string.sign_in),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(bottom = 24.dp),
    )
}

@Composable
private fun LoginWithEmailForm(
    email: MutableState<String>,
    state: LoginScreenState,
    onSendLinkClick: () -> Unit,
) {
    Input(
        modifier = Modifier.padding(24.dp, 8.dp),
        label = stringResource(id = com.kape.ui.R.string.enter_email),
        maskInput = false,
        keyboard = KeyboardType.Email,
        content = email,
    )
    PrimaryButton(
        text = stringResource(id = com.kape.ui.R.string.send_link),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        onClick = onSendLinkClick,
    )
    getErrorMessage(state = state)?.let { message ->
        ErrorCard(
            content = message,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
    }
}

private const val LOGO_WIDTH_FRACTION = 0.45f

@Composable
private fun getErrorMessage(state: LoginScreenState): String? =
    when (state.error) {
        LoginError.Expired -> stringResource(id = com.kape.ui.R.string.error_account_expired)
        LoginError.Failed,
        LoginError.ReceiptFailed,
        LoginError.Invalid,
        LoginError.Throttled,
        -> stringResource(id = com.kape.ui.R.string.error_missing_email)

        LoginError.ServiceUnavailable -> stringResource(id = com.kape.ui.R.string.error_operation_failed)
        null -> null
    }