package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import com.kape.signup.R
import com.kape.ui.elements.ButtonProperties
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.UiResources
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.Space

@Composable
fun SignupErrorScreen(toLogin: () -> Unit) {
    val buttonProperties =
        ButtonProperties(
            label = stringResource(id = R.string.error_account_creation_action).toUpperCase(
                Locale.current
            ),
            enabled = true,
            onClick = {
                toLogin.invoke()
            }
        )

    Box(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(Height.DEFAULT))
        Image(
            painter = painterResource(id = UiResources.bigAppLogo),
            contentDescription = stringResource(id = R.string.logo),
            modifier = Modifier
                .padding(Space.HUGE)
                .align(Alignment.TopCenter)
        )
        Column(modifier = Modifier.align(Alignment.Center)) {
            Image(
                painter = painterResource(id = R.drawable.ic_red_warning),
                contentDescription = stringResource(id = R.string.logo),
                modifier = Modifier.align(CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            Text(
                text = stringResource(id = R.string.error_account_creation_title),
                fontSize = FontSize.Title,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(horizontal = Space.MEDIUM)
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            Text(
                text = stringResource(id = R.string.error_account_creation_message),
                fontSize = FontSize.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .padding(horizontal = Space.MEDIUM)
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            PrimaryButton(
                modifier = Modifier.padding(Space.MEDIUM, Space.MINI),
                properties = buttonProperties
            )
        }
    }
}