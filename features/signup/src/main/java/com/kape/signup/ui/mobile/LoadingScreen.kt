package com.kape.signup.ui.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.utils.LocalColors

@Composable
fun LoadingScreen() = Screen {
    Box(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(48.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_logo_large),
            contentDescription = "logo",
            modifier = Modifier
                .padding(48.dp)
                .align(Alignment.TopCenter),
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 520.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .size(48.dp),
                color = LocalColors.current.primary,
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(id = R.string.loading_text),
                fontSize = 16.sp,
            )
        }
    }
}