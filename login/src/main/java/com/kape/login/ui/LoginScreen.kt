package com.kape.login.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import com.kape.core.InternetConnectionState
import com.kape.login.R
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.utils.connectivityState
import com.kape.uicomponents.components.*
import com.kape.uicomponents.theme.DarkGreen20
import com.kape.uicomponents.theme.Space
import com.kape.uicomponents.theme.Typography
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.viewModel

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun LoginScreen() {

    val viewModel: LoginViewModel by viewModel()
    val state by remember(viewModel) { viewModel.loginState }.collectAsState()
    val connection by connectivityState()
    val isConnected = connection === InternetConnectionState.Connected

    val currentContext = LocalContext.current
    val noNetworkMessage = stringResource(id = R.string.no_internet)

    val userProperties = InputFieldProperties(label = stringResource(id = R.string.enter_username), maskInput = false)
    val passProperties =
        InputFieldProperties(label = stringResource(id = R.string.enter_password), error = getErrorMessage(state = state), maskInput = true)
    val buttonProperties =
        ButtonProperties(label = stringResource(id = R.string.login).toUpperCase(Locale.current), enabled = true, onClick = {
            if (isConnected) {
                viewModel.login(userProperties.content, passProperties.content)
            } else {
                Toast.makeText(currentContext, noNetworkMessage, Toast.LENGTH_SHORT).show()
            }
        })

    if (state.flowCompleted) {
        Toast.makeText(LocalContext.current, "Login Successful", Toast.LENGTH_LONG).show()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isConnected) {
            NoNetworkBanner(noNetworkMessage = stringResource(id = R.string.no_internet))
        }
        Image(painter = painterResource(id = UiResources.bigAppLogo),
            contentDescription = "logo",
            modifier = Modifier
                .padding(start = Space.CENT_FIFTY, top = Space.BIGGER, bottom = Space.MEDIUM, end = Space.CENT_FIFTY))
        Text(text = stringResource(id = R.string.sign_in),
            style = Typography.subtitle1,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(bottom = Space.MEDIUM))
        InputField(modifier = Modifier.padding(Space.MEDIUM, Space.SMALL), properties = userProperties)
        InputField(modifier = Modifier.padding(Space.MEDIUM, Space.SMALL), properties = passProperties)
        PrimaryButton(modifier = Modifier.padding(Space.MEDIUM, Space.MINI), properties = buttonProperties)
        Text(text = stringResource(id = R.string.login_with_receipt).toUpperCase(Locale.current), color = DarkGreen20, modifier = Modifier
            .align(CenterHorizontally)
            .padding(Space.NORMAL, Space.NORMAL, Space.NORMAL, Space.SMALL)
            .clickable {
                Toast
                    .makeText(currentContext, "NOT IMPLEMENTED YET", Toast.LENGTH_SHORT)
                    .show()
            })
        Text(text = stringResource(id = R.string.login_with_magic_link).toUpperCase(Locale.current),
            color = DarkGreen20,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(Space.NORMAL, Space.SMALL, Space.NORMAL, Space.NORMAL)
                .clickable {
                    Toast
                        .makeText(currentContext, "NOT IMPLEMENTED YET", Toast.LENGTH_SHORT)
                        .show()
                })
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
