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
import com.kape.signup.R
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.elements.InputField
import com.kape.ui.elements.InputFieldProperties
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.Screen
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.Space
import com.kape.ui.utils.LocalColors

@Composable
fun EmailScreen(viewModel: SignupViewModel) = Screen {
    val emailProperties =
        InputFieldProperties(label = stringResource(id = R.string.email_hint), maskInput = false)

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(Height.DEFAULT))
        Image(
            painter = painterResource(id = com.kape.ui.R.drawable.ic_logo_large),
            contentDescription = stringResource(id = R.string.logo),
            modifier = Modifier
                .padding(Space.HUGE)
                .align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(Space.VERY_HUGE))
        Text(
            text = stringResource(id = R.string.email_title),
            fontSize = FontSize.Big,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(Space.NORMAL))
        Text(
            text = stringResource(id = R.string.email_text),
            color = LocalColors.current.outlineVariant,
            fontSize = FontSize.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = Space.MEDIUM),
        )
        Spacer(modifier = Modifier.height(Space.MEDIUM))
        InputField(
            modifier = Modifier.padding(horizontal = Space.MEDIUM),
            properties = emailProperties,
        )
        Spacer(modifier = Modifier.height(Space.NORMAL))
        PrimaryButton(
            text = stringResource(id = R.string.submit),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            viewModel.register(emailProperties.content.value)
        }
    }
}