package com.kape.login.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kape.login.R
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.utils.LoginError
import com.kape.login.utils.LoginScreenState
import com.kape.router.Login
import com.kape.ui.elements.ErrorCard
import com.kape.ui.elements.NoNetworkBanner
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.Screen
import com.kape.ui.text.Input
import com.kape.ui.text.SignInText
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.connectivityState
import com.kape.utils.InternetConnectionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(navController: NavController) = Screen {
    val viewModel: LoginViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.loginState }.collectAsState()
    val connection by connectivityState()
    val isConnected = connection === InternetConnectionState.Connected
    val currentContext = LocalContext.current
    val noNetworkMessage = stringResource(id = R.string.no_internet)

    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    LaunchedEffect(key1 = state) {
        viewModel.checkUserLoggedIn()
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .background(LocalColors.current.background)
            .semantics {
                testTagsAsResourceId = true
            },
    ) {
        if (!isConnected) {
            NoNetworkBanner(noNetworkMessage = stringResource(id = R.string.no_internet))
        }
        Image(
            painter = painterResource(id = com.kape.ui.R.drawable.pia_medium),
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .height(40.dp)
                .fillMaxWidth(),
        )
        SignInText(
            content = stringResource(id = R.string.sign_in),
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(16.dp),
        )
        Input(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag(":LoginScreen:enter_username"),
            label = stringResource(id = R.string.enter_username),
            maskInput = false,
            keyboard = KeyboardType.Text,
            imeAction = ImeAction.Next,
            content = username,
        )
        Input(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag(":LoginScreen:enter_password"),
            label = stringResource(id = R.string.enter_password),
            maskInput = true,
            keyboard = KeyboardType.Password,
            imeAction = ImeAction.Next,
            content = password,
        )
        if (state.loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(CenterHorizontally),
            )
        } else {
            PrimaryButton(
                text = stringResource(id = R.string.login),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag(":LoginScreen:login_button"),
            ) {
                if (isConnected) {
                    viewModel.login(username.value, password.value)
                } else {
                    Toast.makeText(currentContext, noNetworkMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (state.error != null) {
            ErrorCard(
                content = getErrorMessage(state = state) ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }

        Text(
            text = stringResource(id = R.string.login_with_receipt).toUpperCase(Locale.current),
            color = LocalColors.current.primary,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(16.dp, 16.dp, 16.dp, 8.dp)
                .clickable {
                    viewModel.loginWithReceipt(currentContext.packageName)
                },
        )
        Text(
            text = stringResource(id = R.string.login_with_magic_link).toUpperCase(Locale.current),
            color = LocalColors.current.primary,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(16.dp, 8.dp, 16.dp, 16.dp)
                .clickable {
                    navController.navigate(Login.WithEmail)
                },
        )
    }
}

@Composable
private fun getErrorMessage(state: LoginScreenState): String? {
    return when (state.error) {
        LoginError.Expired -> "account expired flow" // TODO: handle when signup module is built
        LoginError.Failed -> stringResource(id = R.string.error_username_password_invalid)
        LoginError.Invalid -> stringResource(id = R.string.error_missing_credentials)
        LoginError.Throttled -> stringResource(id = R.string.error_throttled)
        LoginError.ServiceUnavailable -> stringResource(id = R.string.error_operation_failed)
        null -> null
    }
}

@Preview
@Composable
fun ShowLoginScreen() {
    LoginScreen(rememberNavController())
}