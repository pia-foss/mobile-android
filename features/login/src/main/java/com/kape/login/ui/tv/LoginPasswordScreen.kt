package com.kape.login.ui.tv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.unit.dp
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.ui.vm.tv.LoginPasswordViewModel
import com.kape.login.utils.EXPIRED
import com.kape.login.utils.FAILED
import com.kape.login.utils.IDLE
import com.kape.login.utils.INVALID
import com.kape.login.utils.LOADING
import com.kape.login.utils.LoginError
import com.kape.login.utils.SERVICE_UNAVAILABLE
import com.kape.login.utils.SUCCESS
import com.kape.login.utils.THROTTLED
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.Input
import com.kape.ui.tv.text.EnterUsernameScreenTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginPasswordScreen() = Screen {
    val loginPasswordViewModel: LoginPasswordViewModel = koinViewModel()
    val loginViewModel: LoginViewModel = koinViewModel()

    val evaluatePasswordError = remember { mutableStateOf(false) }
    val loginState by remember(loginViewModel) { loginViewModel.loginState }.collectAsState()

    when (loginState) {
        IDLE -> {
            ShowLoginPasswordScreen(
                loginViewModel = loginViewModel,
                loginPasswordViewModel = loginPasswordViewModel,
                evaluatePasswordError = evaluatePasswordError,
                isLoggingIn = false,
                loginError = null,
            )
        }
        LOADING -> {
            ShowLoginPasswordScreen(
                loginViewModel = loginViewModel,
                loginPasswordViewModel = loginPasswordViewModel,
                evaluatePasswordError = evaluatePasswordError,
                isLoggingIn = true,
                loginError = null,
            )
        }
        SUCCESS -> {
            // Do nothing. The viewmodel is handling the navigation on success.
        }
        THROTTLED,
        FAILED,
        EXPIRED,
        INVALID,
        SERVICE_UNAVAILABLE,
        -> {
            evaluatePasswordError.value = true
            ShowLoginPasswordScreen(
                loginViewModel = loginViewModel,
                loginPasswordViewModel = loginPasswordViewModel,
                evaluatePasswordError = evaluatePasswordError,
                isLoggingIn = false,
                loginError = loginState.error,
            )
        }
    }
}

@Composable
fun ShowLoginPasswordScreen(
    loginViewModel: LoginViewModel,
    loginPasswordViewModel: LoginPasswordViewModel,
    evaluatePasswordError: MutableState<Boolean>,
    isLoggingIn: Boolean,
    loginError: LoginError?,
) {
    val loginFailedErrorMessage = stringResource(id = R.string.error_username_password_invalid)
    val loginThrottledErrorMessage = stringResource(id = R.string.error_throttled)
    val passwordErrorMessage = stringResource(id = R.string.error_password_invalid)

    fun showPasswordErrorIfNeeded(): String? {
        if (evaluatePasswordError.value.not()) {
            return null
        }

        loginError?.let {
            return when (it) {
                LoginError.Throttled ->
                    loginThrottledErrorMessage
                LoginError.Invalid,
                LoginError.Failed,
                LoginError.Expired,
                LoginError.ServiceUnavailable,
                ->
                    loginFailedErrorMessage
            }
        }

        return if (loginPasswordViewModel.isValidPassword()) {
            null
        } else {
            passwordErrorMessage
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalColors.current.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isLoggingIn) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    modifier = Modifier.height(80.dp),
                    painter = painterResource(id = R.drawable.ic_logo_large),
                    contentScale = ContentScale.Fit,
                    contentDescription = null,
                )
                CircularProgressIndicator(
                    modifier = Modifier.padding(vertical = 32.dp),
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(64.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_large),
                        contentDescription = null,
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    EnterUsernameScreenTitleText(
                        content = stringResource(id = R.string.login),
                    )
                    Spacer(modifier = Modifier.height(64.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = LocalColors.current.onPrimaryContainer,
                        ),
                    ) {
                        Image(
                            painter = painterResource(id = com.kape.login.R.drawable.ic_tv_onboarding),
                            contentScale = ContentScale.Fit,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 16.dp),
                        )
                    }
                }
            }
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 64.dp)
                    .width(0.5.dp),
                color = LocalColors.current.primaryContainer,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(64.dp),
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 32.dp),
                ) {
                    Column {
                        EnterUsernameScreenTitleText(
                            modifier = Modifier.fillMaxWidth(),
                            content = stringResource(id = R.string.tv_enter_password),
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Input(
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            maskInput = true,
                            keyboard = KeyboardType.Password,
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    evaluatePasswordError.value = true
                                    if (loginPasswordViewModel.isValidPassword()) {
                                        loginViewModel.login(
                                            username = loginPasswordViewModel.getLoginUsername(),
                                            password = loginPasswordViewModel.getPassword().value,
                                        )
                                    }
                                },
                            ),
                            imeAction = ImeAction.Done,
                            platformImeOptions = PlatformImeOptions(
                                privateImeOptions = "horizontalAlignment=right",
                            ),
                            content = loginPasswordViewModel.getPassword(),
                            errorMessage = showPasswordErrorIfNeeded(),
                        )
                    }
                }
                Row(modifier = Modifier.weight(1f)) { }
            }
        }
    }
}