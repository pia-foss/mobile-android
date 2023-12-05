package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.signup.R
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.elements.InputField
import com.kape.ui.elements.InputFieldProperties
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.Screen
import com.kape.ui.utils.LocalColors

@Composable
fun EmailScreen(viewModel: SignupViewModel) = Screen {
    val emailProperties =
        InputFieldProperties(label = stringResource(id = R.string.email_hint), maskInput = false)

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(48.dp))
        Image(
            painter = painterResource(id = com.kape.ui.R.drawable.ic_logo_large),
            contentDescription = stringResource(id = R.string.logo),
            modifier = Modifier
                .padding(48.dp)
                .align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = stringResource(id = R.string.email_title),
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.email_text),
            color = LocalColors.current.outlineVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(24.dp))
        InputField(
            modifier = Modifier.padding(horizontal = 24.dp),
            properties = emailProperties,
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = stringResource(id = R.string.submit),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            viewModel.register(emailProperties.content.value)
        }
    }
}