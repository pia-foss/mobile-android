package com.kape.login.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kape.core.InternetConnectionState
import com.kape.login.R
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.utils.connectivityState
import com.kape.uicomponents.components.*
import com.kape.uicomponents.theme.Typography
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.viewModel

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun LoginScreen() {

    val viewModel: LoginViewModel by viewModel()

    val state by remember(viewModel) { viewModel.loginState }.collectAsState()

    val userProperties = InputFieldProperties(label = stringResource(id = R.string.enter_username), maskInput = false)
    val passProperties =
        InputFieldProperties(label = stringResource(id = R.string.enter_password), error = getErrorMessage(state = state), maskInput = true)
    val buttonProperties = ButtonProperties(label = stringResource(id = R.string.submit), enabled = true, onClick = {
        viewModel.login(userProperties.content, passProperties.content)
    })

    val connection by connectivityState()
    val isConnected = connection === InternetConnectionState.Connected

    if (state.flowCompleted) {
        Toast.makeText(LocalContext.current, "Login Successful", Toast.LENGTH_LONG).show()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isConnected) {
            NoNetworkBanner(noNetworkMessage = stringResource(id = R.string.no_internet))
        }
        Image(painter = painterResource(id = R.drawable.ic_pia_logo),
            contentDescription = "logo",
            modifier = Modifier
                .padding(start = 100.dp, top = 36.dp, bottom = 48.dp, end = 100.dp))
        Text(text = stringResource(id = R.string.sign_in),
            style = Typography.subtitle1,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(bottom = 48.dp))
        InputField(modifier = Modifier.padding(24.dp, 8.dp), properties = userProperties)
        InputField(modifier = Modifier.padding(24.dp, 8.dp), properties = passProperties)
        PrimaryButton(modifier = Modifier.padding(24.dp, 4.dp), properties = buttonProperties)
    }
}

@Composable
private fun getErrorMessage(state: LoginViewModel.LoginScreenState): String? {
    return when (state.error) {
        LoginViewModel.LoginError.Expired -> "account expired flow" // TODO: handle when signup module is built
        LoginViewModel.LoginError.Failed -> stringResource(id = R.string.error_username_password_invalid)
        LoginViewModel.LoginError.Invalid -> stringResource(id = R.string.error_missing_credentials)
        LoginViewModel.LoginError.Throttled -> stringResource(id = R.string.error_throttled)
        LoginViewModel.LoginError.ServiceUnavailable -> stringResource(id = R.string.error_operation_failed)
        null -> null
    }
}

@Preview
@Composable

fun ShowLoginScreen() {
    LoginScreen()
}
