package com.kape.signup.ui.mobile

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.login.utils.IDLE
import com.kape.login.utils.LoginError
import com.kape.login.utils.LoginScreenState
import com.kape.signup.ui.vm.WelcomeBackViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.ErrorCard
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.SecondaryButton
import com.kape.ui.mobile.text.OnboardingDescriptionText
import com.kape.ui.theme.PiaTypography
import com.kape.ui.theme.PreviewTheme
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun WelcomeBackScreen(viewModel: WelcomeBackViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    WelcomeBackScreenContent(
        state = state,
        onPlayStoreAccountClick = { viewModel.onPlayStoreAccountClicked(context.packageName) },
        onUsernameAndPasswordClick = viewModel::onUsernameAndPasswordClicked,
    )
}

@Composable
private fun WelcomeBackScreenContent(
    state: LoginScreenState = IDLE,
    onPlayStoreAccountClick: () -> Unit,
    onUsernameAndPasswordClick: () -> Unit,
) = Screen {
    val activity = LocalActivity.current
    BackHandler {
        activity?.finish()
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(com.kape.signup.R.drawable.background),
                    alignment = Alignment.TopEnd,
                    alpha = 0.2f,
                ).semantics {
                    testTagsAsResourceId = true
                },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier =
                Modifier
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .padding(horizontal = 20.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Image(
                painter = painterResource(id = com.kape.signup.R.drawable.pia_full_name),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.height(40.dp))
            Image(
                painter = painterResource(id = com.kape.signup.R.drawable.welcome_back_hero),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.welcome_back_title),
                color = LocalColors.current.primary,
                style = PiaTypography.h1.copy(fontSize = 28.sp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            OnboardingDescriptionText(
                content = stringResource(id = R.string.welcome_back_description),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(id = R.string.welcome_back_sign_in_with),
                color = LocalColors.current.onSurface,
                style = PiaTypography.body2,
            )
            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(
                text = stringResource(id = R.string.welcome_back_play_store_account),
                modifier = Modifier.fillMaxWidth(),
                isLoading = state.loading,
            ) {
                onPlayStoreAccountClick()
            }
            if (state.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ErrorCard(
                    content = getErrorMessage(state),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.welcome_back_or),
                color = LocalColors.current.onSurface,
                style = PiaTypography.body2,
            )
            Spacer(modifier = Modifier.height(12.dp))
            SecondaryButton(
                text = stringResource(id = R.string.welcome_back_username_password),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(":SignUpScreen:Login"),
            ) {
                onUsernameAndPasswordClick()
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun getErrorMessage(state: LoginScreenState): String =
    when (state.error) {
        LoginError.ReceiptFailed -> stringResource(id = R.string.error_receipt_login_failed)
        LoginError.Expired -> stringResource(id = R.string.error_account_expired)
        LoginError.Throttled -> stringResource(id = R.string.error_throttled)
        LoginError.ServiceUnavailable -> stringResource(id = R.string.error_operation_failed)
        LoginError.Invalid, LoginError.Failed, null -> ""
    }

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewWelcomeBackScreen() {
    PreviewTheme {
        WelcomeBackScreenContent(
            onPlayStoreAccountClick = {},
            onUsernameAndPasswordClick = {},
        )
    }
}