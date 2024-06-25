package com.kape.dedicatedip.ui.screens.tv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.theme.connectionError
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.theme.statusBarError
import com.kape.ui.tiles.Dialog
import com.kape.ui.tv.elements.PrimaryButton
import com.kape.ui.tv.text.AppBarTitleText
import com.kape.ui.tv.text.DedicatedIpAddressDetailsSubtitle
import com.kape.ui.tv.text.DedicatedIpAddressDetailsTitle
import com.kape.ui.tv.text.DedicatedIpDescription
import com.kape.ui.tv.text.DedicatedIpTitle
import com.kape.ui.tv.text.Input
import com.kape.ui.utils.LocalColors
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun TvDedicatedIpScreen() = Screen {
    val viewModel: DipViewModel = koinViewModel<DipViewModel>().apply {
        loadDedicatedIps()
    }
    val connectionManager: ConnectionManager = koinInject()
    val connectionStatus = connectionManager.connectionStatus.collectAsState()

    val showActivationDialog = remember { mutableStateOf(false) }
    val showDeletionDialog = remember { mutableStateOf(false) }
    val text = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 4.dp,
            color = getTopBarConnectionColor(
                status = connectionStatus.value,
                scheme = LocalColors.current,
            ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 24.dp, end = 32.dp, bottom = 0.dp)
                .background(LocalColors.current.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppBarTitleText(
                    content = stringResource(id = R.string.dedicated_ip),
                    textColor = LocalColors.current.onSurface,
                    isError = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(end = 48.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                ) {
                    if (viewModel.dipList.isEmpty()) {
                        ActivateDedicatedIpContent(
                            viewModel = viewModel,
                            text = text,
                        )
                    } else {
                        DedicatedIpDetailsContent(
                            viewModel = viewModel,
                            showDeletionDialog = showDeletionDialog,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.tv_welcome),
                        contentScale = ContentScale.Fit,
                        contentDescription = null,
                    )
                }
            }
        }
    }

    showActivationDialog.value = viewModel.activationState.value != null
    if (showActivationDialog.value) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.6f),
        ) {
            when (viewModel.activationState.value) {
                DipApiResult.Active -> {
                    Dialog(
                        title = stringResource(id = R.string.you_are_all_set),
                        text = stringResource(id = R.string.your_dedicated_ip_is_now_active),
                        onConfirmButtonText = stringResource(id = R.string.ok),
                        onConfirm = {
                            viewModel.resetActivationState()
                        },
                    )
                }

                DipApiResult.Expired -> {
                    Dialog(
                        title = stringResource(id = R.string.something_went_wrong),
                        text = stringResource(id = R.string.dip_expired_warning),
                        onConfirmButtonText = stringResource(id = R.string.try_again),
                        onDismissButtonText = stringResource(id = R.string.cancel),
                        onConfirm = {
                            viewModel.resetActivationState()
                        },
                        onDismiss = {
                            viewModel.resetActivationState()
                            viewModel.navigateBack()
                        },
                    )
                }

                DipApiResult.Error,
                DipApiResult.Invalid,
                null,
                -> {
                    Dialog(
                        title = stringResource(id = R.string.something_went_wrong),
                        text = stringResource(id = R.string.dip_invalid),
                        onConfirmButtonText = stringResource(id = R.string.try_again),
                        onDismissButtonText = stringResource(id = R.string.cancel),
                        onConfirm = {
                            viewModel.resetActivationState()
                        },
                        onDismiss = {
                            viewModel.resetActivationState()
                            viewModel.navigateBack()
                        },
                    )
                }
            }
        }
    }

    if (showDeletionDialog.value) {
        val server = viewModel.dipList.toList().first()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.6f),
        ) {
            Dialog(
                title = stringResource(id = R.string.account_deletion_dialog_title),
                text = String.format(
                    stringResource(id = R.string.dip_remove_description),
                    server.name,
                    server.dedicatedIp,
                ),
                onConfirmButtonText = stringResource(id = R.string.account_deletion_dialog_positive_action),
                onDismissButtonText = stringResource(id = R.string.cancel),
                onConfirm = {
                    showDeletionDialog.value = false
                    server.dipToken?.let {
                        viewModel.removeDip(it)
                    }
                },
                onDismiss = {
                    showDeletionDialog.value = false
                },
            )
        }
    }
}

@Composable
private fun ActivateDedicatedIpContent(viewModel: DipViewModel, text: MutableState<String>) {
    DedicatedIpTitle(
        modifier = Modifier.fillMaxWidth(),
        content = stringResource(id = R.string.tv_dedicated_ip_title),
    )
    Spacer(modifier = Modifier.height(16.dp))
    DedicatedIpDescription(
        modifier = Modifier.fillMaxWidth(),
        content = stringResource(id = R.string.tv_dedicated_ip_description),
    )
    Spacer(modifier = Modifier.height(16.dp))
    Input(
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(id = R.string.tv_dedicated_ip_textfield),
        maskInput = false,
        keyboard = KeyboardType.Text,
        keyboardActions = KeyboardActions(
            onDone = {
                viewModel.activateDedicatedIp(mutableStateOf(TextFieldValue(text.value)))
            },
        ),
        imeAction = ImeAction.Done,
        platformImeOptions = PlatformImeOptions(
            privateImeOptions = "horizontalAlignment=left",
        ),
        content = text,
    )
}

@Composable
private fun DedicatedIpDetailsContent(
    viewModel: DipViewModel,
    showDeletionDialog: MutableState<Boolean>,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        DedicatedIpAddressDetailsTitle(
            modifier = Modifier.fillMaxWidth(),
            content = stringResource(id = R.string.ip_address),
        )
        Spacer(modifier = Modifier.height(8.dp))
        DedicatedIpAddressDetailsSubtitle(
            modifier = Modifier.fillMaxWidth(),
            content = viewModel.dipList.toList().first().dedicatedIp!!,
        )
        Spacer(modifier = Modifier.height(16.dp))
        DedicatedIpAddressDetailsTitle(
            modifier = Modifier.fillMaxWidth(),
            content = stringResource(id = R.string.location),
        )
        Spacer(modifier = Modifier.height(8.dp))
        DedicatedIpAddressDetailsSubtitle(
            modifier = Modifier.fillMaxWidth(),
            content = viewModel.dipList.toList().first().name,
        )
    }
    Spacer(modifier = Modifier.height(32.dp))
    PrimaryButton(
        text = stringResource(id = R.string.remove_dedicated_ip),
        textAlign = TextAlign.Start,
        focusedContainerColor = LocalColors.current.connectionError(),
    ) {
        showDeletionDialog.value = true
    }
}

@Composable
private fun getTopBarConnectionColor(status: ConnectionStatus, scheme: ColorScheme): Color {
    return when (status) {
        ConnectionStatus.ERROR -> scheme.statusBarError()
        ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
        ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING -> scheme.statusBarDefault(scheme)
        ConnectionStatus.RECONNECTING, ConnectionStatus.CONNECTING -> scheme.statusBarConnecting()
    }
}