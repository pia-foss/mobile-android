package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.ui.R
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.Screen

@Composable
fun SignupErrorScreen(toLogin: () -> Unit) = Screen {
    Box(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(48.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_logo_large),
            contentDescription = stringResource(id = R.string.logo),
            modifier = Modifier
                .padding(48.dp)
                .align(Alignment.TopCenter),
        )
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                Image(
                    painter = painterResource(id = com.kape.signup.R.drawable.ic_red_warning),
                    contentDescription = stringResource(id = R.string.logo),
                    modifier = Modifier.align(CenterHorizontally),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.error_account_creation_title),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(horizontal = 24.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.error_account_creation_message),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .padding(horizontal = 24.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = stringResource(id = R.string.error_account_creation_action),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag(":LoginScreen:login_button"),
                ) {
                    toLogin()
                }
            }
        }
    }
}