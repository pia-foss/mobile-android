package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.signup.R
import com.kape.uicomponents.components.UiResources
import com.kape.uicomponents.theme.*

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(Height.DEFAULT))
        Image(
            painter = painterResource(id = UiResources.bigAppLogo),
            contentDescription = "logo",
            modifier = Modifier
                .padding(Space.HUGE)
                .align(Alignment.TopCenter)
        )
        Column(modifier = Modifier.align(Alignment.Center)) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .size(Square.DEFAULT), color = DarkGreen20
            )
            Spacer(modifier = Modifier.height(Height.DEFAULT))
            Text(
                text = stringResource(id = R.string.loading_text),
                fontSize = FontSize.Big
            )
        }
    }
}