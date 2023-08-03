package com.kape.login.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kape.core.InternetConnectionState
import com.kape.login.R
import com.kape.login.ui.vm.LoginWithEmailViewModel
import com.kape.login.utils.LoginError
import com.kape.login.utils.LoginScreenState
import com.kape.login.utils.connectivityState
import com.kape.router.Login
import com.kape.uicomponents.components.*
import com.kape.uicomponents.theme.Space
import com.kape.uicomponents.theme.Typography
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun LoginWithEmailScreen(navController: NavController) {
    val viewModel: LoginWithEmailViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.loginState }.collectAsState()
    val connection by connectivityState()
    val isConnected = connection === InternetConnectionState.Connected
    val currentContext = LocalContext.current
    val noNetworkMessage = stringResource(id = R.string.no_internet)
    val emailProperties =
        InputFieldProperties(label = stringResource(id = R.string.enter_email), error = getErrorMessage(state = state), maskInput = false)
    val buttonProperties =
        ButtonProperties(label = stringResource(id = R.string.send_link).toUpperCase(Locale.current), enabled = true, onClick = {
            if (isConnected) {
                viewModel.loginWithEmail(emailProperties.content)
            } else {
                Toast.makeText(currentContext, noNetworkMessage, Toast.LENGTH_SHORT).show()
            }
        })

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isConnected) {
            NoNetworkBanner(noNetworkMessage = stringResource(id = R.string.no_internet))
        }
        Image(
            painter = painterResource(id = com.kape.uicomponents.R.drawable.ic_pia_logo),
            contentDescription = "logo",
            modifier = Modifier
                .padding(start = 150.dp, top = 36.dp, bottom = Space.MEDIUM, end = 150.dp),
        )
        Text(
            text = stringResource(id = R.string.sign_in),
            style = Typography.subtitle1,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = Space.MEDIUM),
        )
        InputField(modifier = Modifier.padding(Space.MEDIUM, Space.SMALL), properties = emailProperties)
        PrimaryButton(modifier = Modifier.padding(Space.MEDIUM, Space.MINI), properties = buttonProperties)
    }

    if (state.flowCompleted) {
        val message = stringResource(id = R.string.login_with_magic_link_success)
        LaunchedEffect(key1 = state, block = {
            Toast.makeText(currentContext, message, Toast.LENGTH_SHORT).show()
            navController.navigate(Login.Main)
        })
    }
}

@Composable
private fun getErrorMessage(state: LoginScreenState): String? {
    return when (state.error) {
        LoginError.Expired -> "account expired flow" // TODO: handle when signup module is built
        LoginError.Failed,
        LoginError.Invalid,
        LoginError.Throttled,
        -> stringResource(id = R.string.error_missing_email)
        LoginError.ServiceUnavailable -> stringResource(id = R.string.error_operation_failed)
        null -> null
    }
}

@Composable
@Preview
fun LoginWithEmailPreview() {
    LoginWithEmailScreen(rememberNavController())
}
