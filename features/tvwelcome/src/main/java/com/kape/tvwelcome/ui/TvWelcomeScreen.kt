package com.kape.tvwelcome.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.login.ui.vm.LoginViewModel
import com.kape.tvwelcome.ui.vm.TvWelcomeViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.elements.PrimaryButton
import com.kape.ui.tv.text.WelcomeTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvWelcomeScreen() = Screen {
    val welcomeViewModel: TvWelcomeViewModel = koinViewModel()
    val loginViewModel: LoginViewModel = koinViewModel()
    val initialFocusRequester = FocusRequester()

    LaunchedEffect(key1 = Unit) {
        loginViewModel.checkUserLoggedIn()
        initialFocusRequester.requestFocus()
    }

    BackHandler {
        welcomeViewModel.exitApp()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalColors.current.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(64.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_large),
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            WelcomeTitleText(
                content = stringResource(id = R.string.fast_and_secure_streaming),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = stringResource(id = R.string.login),
                modifier = Modifier.focusRequester(initialFocusRequester),
            ) {
                welcomeViewModel.login()
            }
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(text = stringResource(id = R.string.subscribe_now)) {
                welcomeViewModel.signup()
            }
        }
        Spacer(modifier = Modifier.width(64.dp))
        Column(modifier = Modifier.weight(1f)) {
            Image(
                painter = painterResource(id = R.drawable.tv_welcome),
                contentDescription = null,
            )
        }
    }
}