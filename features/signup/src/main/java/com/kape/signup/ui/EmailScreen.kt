package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.R
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.Screen
import com.kape.ui.text.Input
import com.kape.ui.utils.LocalColors

@Composable
fun EmailScreen(viewModel: SignupViewModel) = Screen {
    val email = remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(modifier = Modifier.widthIn(max = 520.dp)) {
            Spacer(modifier = Modifier.height(48.dp))
            Image(
                painter = painterResource(id = com.kape.ui.R.drawable.ic_logo_large),
                contentDescription = null,
                modifier = Modifier
                    .padding(48.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(64.dp))
            Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
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
            }
            Spacer(modifier = Modifier.height(24.dp))
            Input(
                modifier = Modifier.padding(horizontal = 24.dp),
                label = stringResource(id = R.string.email_hint),
                maskInput = false,
                keyboard = KeyboardType.Email,
                content = email,
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(id = R.string.submit),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                viewModel.register(email.value)
            }
        }
    }
}