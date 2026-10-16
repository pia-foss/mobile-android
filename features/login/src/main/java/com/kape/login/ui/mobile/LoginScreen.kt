package com.kape.login.ui.mobile

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kape.login.R
import com.kape.login.ui.vm.LoginViewModel
import com.kape.login.utils.LoginError
import com.kape.login.utils.LoginScreenState
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.ErrorCard
import com.kape.ui.mobile.elements.NoNetworkBanner
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.Input
import com.kape.ui.mobile.text.SignInText
import com.kape.ui.tiles.Dialog
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen() =
    Screen {
        val viewModel: LoginViewModel = koinViewModel()

        val state by remember(viewModel) { viewModel.state }.collectAsState()
        val isConnected by remember(viewModel) { viewModel.isConnected }.collectAsState()
        val showSupportDialog by remember(viewModel) { viewModel.showSupportDialog }.collectAsState()
        val currentContext = LocalContext.current
        val noNetworkMessage = stringResource(id = com.kape.ui.R.string.no_internet)

        val username = rememberSaveable { mutableStateOf("") }
        // Not saveable: saved instance state may be persisted to disk
        val password = remember { mutableStateOf("") }

        val onLoginClick: () -> Unit = {
            if (isConnected) {
                viewModel.login(username.value, password.value)
            } else {
                Toast
                    .makeText(currentContext, noNetworkMessage, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        val form: @Composable () -> Unit = {
            LoginForm(
                username = username,
                password = password,
                state = state,
                showLoginWithReceipt = viewModel.shouldShowLoginWithReceiptButton,
                onLoginClick = onLoginClick,
                onLoginWithReceiptClick = { viewModel.loginWithReceipt(currentContext.packageName) },
                onLoginWithMagicLinkClick = viewModel::navigateToLoginWithEmail,
            )
        }

        AdaptiveSplitContent(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                    .semantics {
                        testTagsAsResourceId = true
                    },
            header = { isCompact ->
                // Above everything in portrait; beside the form when wide
                if (isCompact && !isConnected) {
                    NoNetworkBanner(noNetworkMessage = noNetworkMessage)
                }
                LoginHeader()
            },
            body = { isCompact ->
                if (!isCompact) {
                    if (!isConnected) {
                        NoNetworkBanner(noNetworkMessage = noNetworkMessage)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                form()
            },
        )

        if (showSupportDialog) {
            Dialog(
                title = stringResource(id = com.kape.ui.R.string.login_support_dialog_title),
                text = stringResource(id = com.kape.ui.R.string.login_support_dialog_message),
                onConfirmButtonText = stringResource(id = com.kape.ui.R.string.drawer_item_title_contact_support),
                onDismissButtonText = stringResource(id = com.kape.ui.R.string.cancel),
                onConfirm = {
                    viewModel.getSupportTicketUrl()?.let { url ->
                        val launchIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        currentContext.startActivity(launchIntent)
                    }
                    viewModel.onSupportDialogDismissed()
                },
                onDismiss = {
                    viewModel.onSupportDialogDismissed()
                },
            )
        }
    }

@Composable
private fun LoginHeader() {
    Image(
        painter = painterResource(id = com.kape.ui.R.drawable.pia_medium),
        contentDescription = null,
        modifier =
            Modifier
                .padding(16.dp)
                .height(40.dp)
                .fillMaxWidth(),
    )
    SignInText(
        content = stringResource(id = com.kape.ui.R.string.sign_in),
        modifier = Modifier.padding(16.dp),
    )
}

@Composable
private fun LoginForm(
    username: MutableState<String>,
    password: MutableState<String>,
    state: LoginScreenState,
    showLoginWithReceipt: Boolean,
    onLoginClick: () -> Unit,
    onLoginWithReceiptClick: () -> Unit,
    onLoginWithMagicLinkClick: () -> Unit,
) {
    Input(
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
        label = stringResource(id = com.kape.ui.R.string.enter_username),
        maskInput = false,
        keyboard = KeyboardType.Text,
        imeAction = ImeAction.Next,
        content = username,
        testTag = ":LoginScreen:enter_username",
    )
    Input(
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
        label = stringResource(id = com.kape.ui.R.string.enter_password),
        maskInput = true,
        keyboard = KeyboardType.Password,
        imeAction = ImeAction.Next,
        content = password,
        testTag = ":LoginScreen:enter_password",
    )
    if (state.loading) {
        Spacer(modifier = Modifier.height(16.dp))
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
    } else {
        PrimaryButton(
            text = stringResource(id = com.kape.ui.R.string.login),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag(":LoginScreen:login_button"),
            onClick = onLoginClick,
        )
    }

    if (state.error != null) {
        ErrorCard(
            content = getErrorMessage(state = state) ?: "",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag(":LoginScreen:login_error"),
        )
    }

    if (showLoginWithReceipt) {
        Text(
            text =
                stringResource(id = com.kape.ui.R.string.login_with_receipt).toUpperCase(
                    Locale.current,
                ),
            color = LocalColors.current.primary,
            modifier =
                Modifier
                    .padding(16.dp, 16.dp, 16.dp, 8.dp)
                    .clickable(onClick = onLoginWithReceiptClick),
        )
    }
    Text(
        text =
            stringResource(id = com.kape.ui.R.string.login_with_magic_link).toUpperCase(
                Locale.current,
            ),
        color = LocalColors.current.primary,
        modifier =
            Modifier
                .padding(16.dp, 8.dp, 16.dp, 16.dp)
                .clickable(onClick = onLoginWithMagicLinkClick),
    )
}

@Composable
private fun getErrorMessage(state: LoginScreenState): String? =
    when (state.error) {
        LoginError.Expired -> stringResource(com.kape.ui.R.string.error_account_expired)
        LoginError.Failed -> stringResource(id = com.kape.ui.R.string.error_username_password_invalid)
        LoginError.ReceiptFailed -> stringResource(id = com.kape.ui.R.string.error_receipt_login_failed)
        LoginError.Invalid -> stringResource(id = com.kape.ui.R.string.error_missing_credentials)
        LoginError.Throttled -> stringResource(id = com.kape.ui.R.string.error_throttled)
        LoginError.ServiceUnavailable -> stringResource(id = com.kape.ui.R.string.error_operation_failed)
        null -> null
    }