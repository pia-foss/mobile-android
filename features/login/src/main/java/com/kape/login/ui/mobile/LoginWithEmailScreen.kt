package com.kape.login.ui.mobile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kape.login.ui.vm.mobile.LoginWithEmailViewModel
import com.kape.login.utils.LoginError
import com.kape.login.utils.LoginScreenState
import com.kape.router.Login
import com.kape.ui.mobile.elements.InsetsColumn
import com.kape.ui.mobile.elements.NoNetworkBanner
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.Input
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginWithEmailScreen(navController: NavController) = Screen {
    val viewModel: LoginWithEmailViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.loginState }.collectAsState()
    val isConnected by remember(viewModel) { viewModel.isConnected }.collectAsState()
    val currentContext = LocalContext.current
    val noNetworkMessage = stringResource(id = com.kape.ui.R.string.no_internet)
    val email = remember { mutableStateOf("") }

    InsetsColumn {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                if (!isConnected) {
                    NoNetworkBanner(noNetworkMessage = stringResource(id = com.kape.ui.R.string.no_internet))
                }
                Image(
                    painter = painterResource(id = com.kape.ui.R.drawable.ic_logo_large),
                    contentDescription = "logo",
                    modifier = Modifier
                        .padding(start = 150.dp, top = 36.dp, bottom = 24.dp, end = 150.dp),
                )
                Text(
                    text = stringResource(id = com.kape.ui.R.string.sign_in),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 24.dp),
                )
                Input(
                    modifier = Modifier.padding(24.dp, 8.dp),
                    label = stringResource(id = com.kape.ui.R.string.enter_email),
                    maskInput = false,
                    keyboard = KeyboardType.Email,
                    content = email,
                )

                PrimaryButton(
                    text = stringResource(id = com.kape.ui.R.string.send_link),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    if (isConnected) {
                        viewModel.loginWithEmail(email.value)
                    } else {
                        Toast.makeText(currentContext, noNetworkMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    if (state.flowCompleted) {
        val message = stringResource(id = com.kape.ui.R.string.login_with_magic_link_success)
        LaunchedEffect(
            key1 = state,
            block = {
                Toast.makeText(currentContext, message, Toast.LENGTH_SHORT).show()
                navController.navigate(Login.WithCredentials)
            },
        )
    }
}

@Composable
private fun getErrorMessage(state: LoginScreenState): String? {
    return when (state.error) {
        LoginError.Expired -> "account expired flow" // TODO: handle when signup module is built
        LoginError.Failed,
        LoginError.Invalid,
        LoginError.Throttled,
        -> stringResource(id = com.kape.ui.R.string.error_missing_email)

        LoginError.ServiceUnavailable -> stringResource(id = com.kape.ui.R.string.error_operation_failed)
        null -> null
    }
}

@Composable
@Preview
fun LoginWithEmailPreview() {
    LoginWithEmailScreen(rememberNavController())
}