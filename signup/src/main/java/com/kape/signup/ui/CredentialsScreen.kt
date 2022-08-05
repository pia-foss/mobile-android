package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import com.kape.signup.R
import com.kape.signup.models.Credentials
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.uicomponents.components.ButtonProperties
import com.kape.uicomponents.components.PrimaryButton
import com.kape.uicomponents.components.UiResources
import com.kape.uicomponents.theme.*

@Composable
fun CredentialsScreen(viewModel: SignupViewModel, credentials: Credentials) {

    val buttonProperties =
        ButtonProperties(
            label = stringResource(id = R.string.get_started).toUpperCase(Locale.current),
            enabled = true,
            onClick = {
                viewModel.completeSubscription()
            })

    Column(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = UiResources.bigAppLogo),
            contentDescription = stringResource(id = R.string.logo),
            modifier = Modifier
                .padding(Space.NORMAL)
                .height(Height.SMALL_LOGO)
                .align(Alignment.CenterHorizontally)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_complete_redeem),
            contentDescription = stringResource(id = R.string.logo),
            modifier = Modifier
                .padding(Space.NORMAL)
                .height(Height.BIG_LOGO)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(id = R.string.credentials_title),
            fontSize = FontSize.Title,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(Space.NORMAL))
        Text(
            text = stringResource(id = R.string.credentials_text),
            color = Grey55,
            fontSize = FontSize.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = Space.MEDIUM)
        )
        Spacer(modifier = Modifier.height(Space.NORMAL))
        Text(
            text = stringResource(id = R.string.credentials_hint),
            color = Grey55,
            fontSize = FontSize.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = Space.MEDIUM)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Space.MEDIUM, end = Space.MEDIUM, top = Space.NORMAL)
                .border(1.dp, shape = OutlineBackground, color = Grey85)
                .padding(Space.NORMAL)
        ) {
            Text(
                text = stringResource(id = R.string.username),
                color = Grey55,
                fontSize = FontSize.Normal
            )
            Text(text = credentials.username, fontSize = FontSize.Title)
        }
        Spacer(modifier = Modifier.height(Space.SMALL))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Space.MEDIUM, end = Space.MEDIUM, bottom = Space.NORMAL)
                .border(1.dp, shape = OutlineBackground, color = Grey85)
                .padding(Space.NORMAL)
        ) {
            Text(
                text = stringResource(id = R.string.password),
                color = Grey55,
                fontSize = FontSize.Normal
            )
            Text(text = credentials.password, fontSize = FontSize.Title)
        }
        Spacer(modifier = Modifier.height(Space.MINI))
        PrimaryButton(
            modifier = Modifier.padding(horizontal = Space.MEDIUM),
            properties = buttonProperties
        )
    }
}