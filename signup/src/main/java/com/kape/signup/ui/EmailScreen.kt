package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.navigation.NavController
import com.kape.signup.R
import com.kape.uicomponents.components.*
import com.kape.uicomponents.theme.FontSize
import com.kape.uicomponents.theme.Grey55
import com.kape.uicomponents.theme.Height
import com.kape.uicomponents.theme.Space

@Composable
fun EmailScreen(navController: NavController) {

    val emailProperties = InputFieldProperties(label = stringResource(id = R.string.email_hint), maskInput = false)
    val buttonProperties =
        ButtonProperties(label = stringResource(id = R.string.submit).toUpperCase(Locale.current), enabled = true, onClick = {
            // TODO: implement actual user registration with backend
        })

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(Height.DEFAULT))
        Image(
            painter = painterResource(id = UiResources.bigAppLogo),
            contentDescription = stringResource(id = R.string.logo),
            modifier = Modifier
                .padding(Space.HUGE)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(Space.VERY_HUGE))
        Text(
            text = stringResource(id = R.string.email_title),
            fontSize = FontSize.Big,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(Space.NORMAL))
        Text(
            text = stringResource(id = R.string.email_text),
            color = Grey55,
            fontSize = FontSize.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = Space.MEDIUM)
        )
        Spacer(modifier = Modifier.height(Space.MEDIUM))
        InputField(modifier = Modifier.padding(horizontal = Space.MEDIUM), properties = emailProperties)
        Spacer(modifier = Modifier.height(Space.NORMAL))
        PrimaryButton(modifier = Modifier.padding(horizontal = Space.MEDIUM), properties = buttonProperties)
    }
}