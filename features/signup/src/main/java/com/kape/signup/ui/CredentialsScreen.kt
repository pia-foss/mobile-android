package com.kape.signup.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.theme.OutlineBackground
import com.kape.ui.utils.LocalColors

@Composable
fun CredentialsScreen(viewModel: SignupViewModel, credentials: Credentials) = Screen {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Column(modifier = Modifier.widthIn(max = 520.dp)) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_large),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .height(24.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Image(
                painter = painterResource(id = com.kape.signup.R.drawable.ic_complete_redeem),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .height(80.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
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
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 24.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.credentials_hint),
                    color = LocalColors.current.outlineVariant,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 24.dp),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp)
                        .border(
                            1.dp,
                            shape = OutlineBackground,
                            color = LocalColors.current.outline,
                        )
                        .padding(16.dp)
                        .semantics(mergeDescendants = true) { },
                ) {
                    Text(
                        text = stringResource(id = R.string.username),
                        color = LocalColors.current.outlineVariant,
                        fontSize = 14.sp,
                    )
                    Text(text = credentials.username, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                        .border(
                            1.dp,
                            shape = OutlineBackground,
                            color = LocalColors.current.outline,
                        )
                        .padding(16.dp)
                        .semantics(mergeDescendants = true) { },
                ) {
                    Text(
                        text = stringResource(id = R.string.password),
                        color = LocalColors.current.outlineVariant,
                        fontSize = 14.sp,
                    )
                    Text(text = credentials.password, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            PrimaryButton(
                text = stringResource(id = R.string.get_started),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                viewModel.completeSubscription()
            }
        }
    }
}