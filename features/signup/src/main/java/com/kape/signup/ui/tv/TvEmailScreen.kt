package com.kape.signup.ui.tv

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.R
import com.kape.ui.tv.text.EnterEmailScreenTitleText
import com.kape.ui.tv.text.Input
import com.kape.ui.tv.text.SignUpTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvEmailScreen() {
    val viewModel: SignupViewModel = koinViewModel()
    val email = remember { mutableStateOf("") }
    val emailErrorMessage = stringResource(id = R.string.error_missing_email)
    val evaluateEmailError = remember { mutableStateOf(false) }

    BackHandler {
        viewModel.exitApp()
    }

    fun showEmailErrorIfNeeded(): String? {
        if (evaluateEmailError.value.not()) {
            return null
        }

        return if (viewModel.isValidEmail(email = email.value)) {
            null
        } else {
            emailErrorMessage
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalColors.current.background),
    ) {
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
                SignUpTitleText(
                    content = stringResource(id = R.string.signup),
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
                    EnterEmailScreenTitleText(
                        modifier = Modifier.fillMaxWidth(),
                        content = stringResource(id = R.string.email_title),
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Input(
                        modifier = Modifier,
                        maskInput = false,
                        keyboard = KeyboardType.Email,
                        keyboardActions = KeyboardActions(
                            onNext = {
                                evaluateEmailError.value = true
                                if (viewModel.isValidEmail(email = email.value)) {
                                    viewModel.register(email = email.value)
                                }
                            },
                        ),
                        imeAction = ImeAction.Next,
                        platformImeOptions = PlatformImeOptions(
                            privateImeOptions = "horizontalAlignment=right",
                        ),
                        content = email,
                        errorMessage = showEmailErrorIfNeeded(),
                    )
                }
            }
            Row(modifier = Modifier.weight(1f)) { }
        }
    }
}