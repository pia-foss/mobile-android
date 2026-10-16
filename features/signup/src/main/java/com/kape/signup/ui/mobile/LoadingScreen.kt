package com.kape.signup.ui.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.ui.R
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.utils.LocalColors

@Composable
fun LoadingScreen() =
    Screen {
        AdaptiveSplitContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
            header = { isCompact ->
                SignupLogo(contentDescription = "logo", isCompact = isCompact)
            },
            body = {
                // Centres the progress in the space below the logo, or beside it when wide
                Spacer(modifier = Modifier.weight(1f))
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = LocalColors.current.primary,
                )
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = stringResource(id = R.string.loading_text),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
            },
        )
    }

/** The large logo the signup steps open with, with less room around it beside other content. */
@Composable
internal fun SignupLogo(
    contentDescription: String?,
    isCompact: Boolean,
) {
    Image(
        painter = painterResource(id = R.drawable.ic_logo_large),
        contentDescription = contentDescription,
        modifier = Modifier.padding(if (isCompact) 48.dp else 24.dp),
    )
}