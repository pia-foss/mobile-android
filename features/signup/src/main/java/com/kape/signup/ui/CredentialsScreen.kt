package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import com.kape.signup.R
import com.kape.signup.data.models.Credentials
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.elements.ButtonProperties
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.UiResources
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.OutlineBackground
import com.kape.ui.theme.Space
import com.kape.ui.utils.LocalColors

@Composable
fun CredentialsScreen(viewModel: SignupViewModel, credentials: Credentials) {
    val buttonProperties =
        ButtonProperties(
            label = stringResource(id = R.string.get_started).toUpperCase(Locale.current),
            enabled = true,
            onClick = {
                viewModel.completeSubscription()
            }
        )

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
            color = LocalColors.current.outlineVariant,
            fontSize = FontSize.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = Space.MEDIUM)
        )
        Spacer(modifier = Modifier.height(Space.NORMAL))
        Text(
            text = stringResource(id = R.string.credentials_hint),
            color = LocalColors.current.outlineVariant,
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
                .border(1.dp, shape = OutlineBackground, color = LocalColors.current.outline)
                .padding(Space.NORMAL)
        ) {
            Text(
                text = stringResource(id = R.string.username),
                color = LocalColors.current.outlineVariant,
                fontSize = FontSize.Normal
            )
            Text(text = credentials.username, fontSize = FontSize.Title)
        }
        Spacer(modifier = Modifier.height(Space.SMALL))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Space.MEDIUM, end = Space.MEDIUM, bottom = Space.NORMAL)
                .border(1.dp, shape = OutlineBackground, color = LocalColors.current.outline)
                .padding(Space.NORMAL)
        ) {
            Text(
                text = stringResource(id = R.string.password),
                color = LocalColors.current.outlineVariant,
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