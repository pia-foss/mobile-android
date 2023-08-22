package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.signup.R
import com.kape.ui.elements.UiResources
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.Space
import com.kape.ui.theme.Square
import com.kape.ui.utils.LocalColors

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(Height.DEFAULT))
        Image(
            painter = painterResource(id = UiResources.bigAppLogo),
            contentDescription = "logo",
            modifier = Modifier
                .padding(Space.HUGE)
                .align(Alignment.TopCenter),
        )
        Column(modifier = Modifier.align(Alignment.Center)) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .size(Square.DEFAULT),
                color = LocalColors.current.primary,
            )
            Spacer(modifier = Modifier.height(Height.DEFAULT))
            Text(
                text = stringResource(id = R.string.loading_text),
                fontSize = FontSize.Big,
            )
        }
    }
}