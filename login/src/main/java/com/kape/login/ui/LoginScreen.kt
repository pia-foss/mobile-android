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
import com.kape.login.R
import com.kape.login.ui.components.ButtonProperties
import com.kape.login.ui.components.InputField
import com.kape.login.ui.components.InputFieldProperties
import com.kape.login.ui.components.PrimaryButton
import com.kape.login.ui.theme.Typography
import com.kape.login.ui.vm.LoginViewModel
import org.koin.androidx.compose.viewModel

@Composable
fun LoginScreen() {

    val viewModel: LoginViewModel by viewModel()

    val state by remember(viewModel) { viewModel.loginState }.collectAsState()

    val userProperties = InputFieldProperties(label = stringResource(id = R.string.enter_username), maskInput = false)
    val passProperties =
        InputFieldProperties(label = stringResource(id = R.string.enter_password), error = state.error, maskInput = true)
    val buttonProperties = ButtonProperties(label = stringResource(id = R.string.submit), enabled = true, onClick = {
        viewModel.login(userProperties.content, passProperties.content)
    })

    if (state.flowCompleted) {
        Toast.makeText(LocalContext.current, "Login Successful", Toast.LENGTH_LONG).show()
    }

    passProperties.error = state.error

    Column(modifier = Modifier.fillMaxSize()) {
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

@Preview
@Composable

fun ShowLoginScreen() {
    LoginScreen()
}
