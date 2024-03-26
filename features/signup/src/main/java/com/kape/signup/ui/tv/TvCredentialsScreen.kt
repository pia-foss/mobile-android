package com.kape.signup.ui.tv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.signup.data.models.Credentials
import com.kape.signup.ui.vm.SignupViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.theme.OutlineBackground
import com.kape.ui.tv.elements.PrimaryButton
import com.kape.ui.tv.text.SignUpTitleText
import com.kape.ui.tv.text.SignupSuccessDescriptionText
import com.kape.ui.tv.text.SignupSuccessTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvCredentialsScreen(credentials: Credentials) = Screen {
    val viewModel: SignupViewModel = koinViewModel()
    val initialFocusRequester = FocusRequester()

    LaunchedEffect(key1 = Unit) {
        initialFocusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalColors.current.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(64.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_large),
                    contentDescription = null,
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                SignUpTitleText(
                    content = stringResource(id = R.string.success),
                )
                Spacer(modifier = Modifier.height(64.dp))
                Card(
                    modifier = Modifier
                        .fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LocalColors.current.onPrimaryContainer,
                    ),
                ) {
                    Image(
                        painter = painterResource(id = com.kape.signup.R.drawable.ic_tv_signup_success),
                        contentScale = ContentScale.Fit,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 16.dp),
                    )
                }
            }
        }
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 64.dp)
                .width(0.5.dp),
            color = LocalColors.current.primaryContainer,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(45.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SignupSuccessTitleText(
                content = stringResource(id = R.string.credentials_title),
            )
            Spacer(modifier = Modifier.height(16.dp))
            SignupSuccessDescriptionText(
                content = stringResource(id = R.string.credentials_text),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
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
                Text(text = credentials.username, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
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
                Text(text = credentials.password, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = stringResource(id = R.string.get_started),
                modifier = Modifier.focusRequester(initialFocusRequester),
            ) {
                viewModel.completeSubscription()
            }
        }
    }
}