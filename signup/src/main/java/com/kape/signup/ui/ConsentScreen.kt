package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.toUpperCase
import androidx.navigation.NavController
import com.kape.signup.R
import com.kape.uicomponents.components.ButtonProperties
import com.kape.uicomponents.components.PrimaryButton
import com.kape.uicomponents.components.SecondaryButton
import com.kape.uicomponents.theme.FontSize
import com.kape.uicomponents.theme.Space

@Composable
fun ConsentScreen(navController: NavController) {

    val showMoreInfo = remember { mutableStateOf(false) }

    val acceptProperties =
        ButtonProperties(label = stringResource(id = R.string.accept).toUpperCase(Locale.current), enabled = true, onClick = {
            // TODO: handle accept click
        })
    val declineProperties =
        ButtonProperties(label = stringResource(id = R.string.no_thanks).toUpperCase(Locale.current), enabled = true, onClick = {
            // TODO: handle no thanks click
        })

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_consent),
                contentDescription = stringResource(id = R.string.logo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Space.MEDIUM)
            )
            Text(
                text = stringResource(id = R.string.consent_title),
                fontSize = FontSize.Big,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            Text(
                text = stringResource(id = R.string.consent_message),
                fontSize = FontSize.Normal,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(Space.MEDIUM)
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            Text(
                text = stringResource(id = R.string.find_out_more).uppercase(),
                fontSize = FontSize.Small,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        showMoreInfo.value = true
                    }
            )
            Spacer(modifier = Modifier.height(Space.NORMAL))
            PrimaryButton(modifier = Modifier.padding(Space.MEDIUM, Space.MINI), properties = acceptProperties)
            SecondaryButton(modifier = Modifier.padding(Space.MEDIUM, Space.MINI), properties = declineProperties)
        }

        if (showMoreInfo.value) {
            AlertDialog(
                onDismissRequest = { showMoreInfo.value = false },
                text = { Text(text = stringResource(id = R.string.consent_more_info)) },
                buttons = {}, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}