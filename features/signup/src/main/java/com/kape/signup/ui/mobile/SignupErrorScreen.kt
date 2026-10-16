package com.kape.signup.ui.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.ui.R
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen

@Composable
fun SignupErrorScreen(toLogin: () -> Unit) =
    Screen {
        AdaptiveSplitContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
            header = { isCompact ->
                SignupLogo(contentDescription = stringResource(id = R.string.logo), isCompact = isCompact)
            },
            body = {
                // Centres the message in the space below the logo, or beside it when wide
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(id = com.kape.signup.R.drawable.ic_red_warning),
                    contentDescription = stringResource(id = R.string.logo),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.error_account_creation_title),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.error_account_creation_message),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = stringResource(id = R.string.error_account_creation_action),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag(":LoginScreen:login_button"),
                ) {
                    toLogin()
                }
                Spacer(modifier = Modifier.weight(1f))
            },
        )
    }