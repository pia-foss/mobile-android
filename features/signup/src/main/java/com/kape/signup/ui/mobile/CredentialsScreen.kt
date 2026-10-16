package com.kape.signup.ui.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.signup.data.models.Credentials
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.theme.OutlineBackground
import com.kape.ui.utils.LocalColors

@Composable
fun CredentialsScreen(
    viewModel: SignupViewModel,
    credentials: Credentials,
) = Screen {
    AdaptiveSplitContent(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
        header = {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_large),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .height(24.dp),
            )
            Image(
                painter = painterResource(id = com.kape.signup.R.drawable.ic_complete_redeem),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .height(80.dp),
            )
            Text(
                text = stringResource(id = R.string.credentials_title),
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.credentials_text),
                color = LocalColors.current.outlineVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 24.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
        },
        body = {
            Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
                Text(
                    text = stringResource(id = R.string.credentials_hint),
                    color = LocalColors.current.outlineVariant,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = 24.dp),
                )
                CredentialBox(
                    label = stringResource(id = R.string.username),
                    value = credentials.username,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                CredentialBox(
                    label = stringResource(id = R.string.password),
                    value = credentials.password,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            PrimaryButton(
                text = stringResource(id = R.string.get_started),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                viewModel.completeSubscription()
            }
        },
    )
}

@Composable
private fun CredentialBox(
    label: String,
    value: String,
    modifier: Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    shape = OutlineBackground,
                    color = LocalColors.current.outline,
                ).padding(16.dp)
                .semantics(mergeDescendants = true) { },
    ) {
        Text(
            text = label,
            color = LocalColors.current.outlineVariant,
            fontSize = 14.sp,
        )
        Text(text = value, fontSize = 18.sp)
    }
}