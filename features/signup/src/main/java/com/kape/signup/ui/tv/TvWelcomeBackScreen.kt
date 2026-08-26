package com.kape.signup.ui.tv

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.kape.login.utils.IDLE
import com.kape.login.utils.LoginScreenState
import com.kape.signup.ui.mobile.getErrorMessage
import com.kape.signup.ui.vm.WelcomeBackViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.ErrorCard
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.theme.PiaTypography
import com.kape.ui.theme.PreviewTheme
import com.kape.ui.tv.elements.PrimaryButton
import com.kape.ui.tv.text.WelcomeTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvWelcomeBackScreen(viewModel: WelcomeBackViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    TvWelcomeBackScreenContent(
        state = state,
        onPlayStoreAccountClick = { viewModel.onPlayStoreAccountClicked(context.packageName) },
        onUsernameAndPasswordClick = viewModel::onUsernameAndPasswordClicked,
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvWelcomeBackScreenContent(
    state: LoginScreenState = IDLE,
    onPlayStoreAccountClick: () -> Unit,
    onUsernameAndPasswordClick: () -> Unit,
) = Screen {
    val activity = LocalActivity.current
    val initialFocusRequester = remember { FocusRequester() }

    BackHandler {
        activity?.finish()
    }

    LaunchedEffect(key1 = Unit) {
        initialFocusRequester.requestFocus()
    }

    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .background(LocalColors.current.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(64.dp),
        ) {
            Image(
                painter = painterResource(id = com.kape.signup.R.drawable.pia_full_name),
                contentDescription = null,
                modifier =
                    Modifier
                        .width(100.dp)
                        .height(40.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            WelcomeTitleText(
                content = stringResource(id = R.string.welcome_back_title),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.welcome_back_description),
                textAlign = TextAlign.Start,
                color = LocalColors.current.onSurface,
                style = PiaTypography.subtitle2,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(id = R.string.welcome_back_sign_in_with),
                textAlign = TextAlign.Start,
                color = LocalColors.current.onSurface,
                style = PiaTypography.body3,
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (state.loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                PrimaryButton(
                    text = stringResource(id = R.string.welcome_back_play_store_account),
                    modifier = Modifier.focusRequester(initialFocusRequester),
                ) {
                    onPlayStoreAccountClick()
                }
            }
            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                ErrorCard(
                    content = getErrorMessage(state),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(
                text = stringResource(id = R.string.welcome_back_username_password),
            ) {
                onUsernameAndPasswordClick()
            }
        }
        Spacer(modifier = Modifier.width(64.dp))
        Column(modifier = Modifier.weight(1f)) {
            Image(
                painter = painterResource(id = com.kape.signup.R.drawable.tv_welcome_back_hero),
                contentDescription = null,
            )
        }
    }
}

@Preview(name = "TV", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 960, heightDp = 540)
@Composable
private fun PreviewTvWelcomeBackScreen() {
    PreviewTheme(isTv = true) {
        TvWelcomeBackScreenContent(
            onPlayStoreAccountClick = {},
            onUsernameAndPasswordClick = {},
        )
    }
}