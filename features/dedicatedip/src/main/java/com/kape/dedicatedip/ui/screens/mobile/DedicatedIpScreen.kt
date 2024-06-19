package com.kape.dedicatedip.ui.screens.mobile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.dedicatedip.R
import com.kape.dedicatedip.ui.vm.DipViewModel
import com.kape.dedicatedip.utils.DipApiResult
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.SecondaryButton
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.theme.PiaTypography
import com.kape.ui.theme.getLatencyColor
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import com.kape.utils.vpnserver.VpnServer
import com.privateinternetaccess.regions.REGIONS_PING_TIMEOUT
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DedicatedIpScreen() = Screen {
    val viewModel: DipViewModel = koinViewModel<DipViewModel>().apply {
        loadDedicatedIps(Locale.getDefault().language)
    }
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = com.kape.ui.R.string.dedicated_ip_title))
    }
    val showToast = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val showSpinner = remember { mutableStateOf(false) }
    val serverForDeletion = remember { mutableStateOf<VpnServer?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                onLeftIconClick = { viewModel.navigateBack() },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
                .semantics {
                    testTagsAsResourceId = true
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .background(LocalColors.current.onPrimary)
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(id = com.kape.ui.R.string.dedicated_ip_title),
                    fontSize = 18.sp,
                    color = LocalColors.current.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = if (viewModel.dipList.isEmpty()) com.kape.ui.R.string.dedicated_ip_description else com.kape.ui.R.string.dip_summary),
                    fontSize = 12.sp,
                    color = LocalColors.current.onSurface,
                )
                if (viewModel.dipList.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        border = BorderStroke(1.dp, color = LocalColors.current.primary),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val text = remember {
                            mutableStateOf(TextFieldValue(""))
                        }
                        Row {
                            TextField(
                                value = text.value,
                                onValueChange = {
                                    text.value = it
                                },
                                placeholder = {
                                    Text(
                                        text = stringResource(id = com.kape.ui.R.string.dedicated_ip_hint),
                                        color = LocalColors.current.onSurface,
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(6.0f)
                                    .testTag(":DedicatedIPScreen:dip_text_field"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedTextColor = LocalColors.current.onSurface,
                                    focusedBorderColor = Color.Transparent,
                                ),
                            )
                            if (showSpinner.value) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(CenterVertically)
                                        .padding(8.dp),
                                )
                            } else {
                                Button(
                                    onClick = {
                                        if (text.value.text.isNotEmpty()) {
                                            showSpinner.value = true
                                            viewModel.activateDedicatedIp(text)
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .align(CenterVertically)
                                        .weight(4.0f)
                                        .testTag(":DedicatedIPScreen:activate_button"),
                                    shape = RoundedCornerShape(4.dp),
                                ) {
                                    Text(
                                        text = stringResource(id = com.kape.ui.R.string.activate),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = LocalColors.current.onPrimary,
                                    )
                                }
                            }
                        }
                    }

                    if (viewModel.showDedicatedIpSignupBanner()) {
                        DedicatedIpSignupBanner(
                            onAcceptTapped = {
                                viewModel.navigateToDedicatedIpPlans()
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (viewModel.dipList.isNotEmpty()) {
                Text(
                    text = stringResource(id = com.kape.ui.R.string.owned_dedicated_ips),
                    fontSize = 18.sp,
                    color = LocalColors.current.primary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    val items = viewModel.dipList
                    items(items.size) { index ->
                        val item = items[index]
                        DipItem(server = item, showDialog, serverForDeletion)
                        Separator()
                    }
                }
            }

            showToast.value = viewModel.activationState.value != null
            showSpinner.value = false

            if (showToast.value) {
                Toast.makeText(
                    context,
                    when (viewModel.activationState.value) {
                        DipApiResult.Active -> stringResource(id = com.kape.ui.R.string.dip_success)
                        DipApiResult.Error -> stringResource(id = com.kape.ui.R.string.dip_invalid)
                        DipApiResult.Expired -> stringResource(id = com.kape.ui.R.string.dip_expired_warning)
                        DipApiResult.Invalid -> stringResource(id = com.kape.ui.R.string.dip_invalid)
                        null -> ""
                    },
                    Toast.LENGTH_LONG,
                ).show()
                viewModel.resetActivationState()
            }

            if (showDialog.value) {
                serverForDeletion.value?.let { server ->
                    server.dipToken?.let { dipToken ->
                        DeleteDipDialog(
                            showDialog = showDialog,
                            server = server,
                            onRemoveClicked = { viewModel.removeDip(dipToken) },
                            onCancelClicked = { serverForDeletion.value = null },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DipItem(
    server: VpnServer,
    showDialog: MutableState<Boolean>,
    serverForDeletion: MutableState<VpnServer?>,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(min = 56.dp),
    ) {
        val (icon, name, latency, ip, removeButton) = createRefs()

        Box(
            modifier = Modifier.constrainAs(icon) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
        ) {
            Icon(
                painter = painterResource(
                    id = getFlagResource(
                        LocalContext.current,
                        server.iso,
                    ),
                ),
                tint = Color.Unspecified,
                contentDescription = null,
                modifier = Modifier
                    .align(Center)
                    .padding(4.dp)
                    .width(32.dp)
                    .height(22.dp)
                    .testTag(":DedicatedIPScreen:dip_flag"),
            )
            Icon(
                painter = painterResource(id = com.kape.ui.R.drawable.ic_dip_badge),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .align(TopEnd)
                    .size(14.dp),
            )
        }

        Text(
            text = server.name,
            fontSize = 14.sp,
            modifier = Modifier
                .constrainAs(name) {
                    start.linkTo(icon.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .testTag(":DedicatedIPScreen:dip_server_name"),
        )

        IconButton(
            onClick = {
                serverForDeletion.value = server
                showDialog.value = true
            },
            modifier = Modifier
                .constrainAs(removeButton) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .testTag(":DedicatedIPScreen:dip_remove_button"),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(id = com.kape.ui.R.string.dip_remove_title),
                modifier = Modifier
                    .width(24.dp)
                    .height(20.dp),
            )
        }

        Text(
            text = server.dedicatedIp ?: "",
            modifier = Modifier.constrainAs(ip) {
                end.linkTo(removeButton.start)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
        )

        Text(
            text = if (server.latency != null && server.latency!!.toInt() < REGIONS_PING_TIMEOUT) {
                stringResource(id = com.kape.ui.R.string.latency_to_format).format(
                    server.latency,
                )
            } else {
                ""
            },
            fontSize = 12.sp,
            color = LocalColors.current.getLatencyColor(server.latency),
            modifier = Modifier.constrainAs(latency) {
                end.linkTo(ip.start, margin = 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
        )
    }
}

@Composable
fun DeleteDipDialog(
    showDialog: MutableState<Boolean>,
    server: VpnServer,
    onRemoveClicked: (String) -> Unit,
    onCancelClicked: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            showDialog.value = false
        },
        title = {
            Text(
                text = stringResource(id = com.kape.ui.R.string.dip_remove_title),
                fontSize = 18.sp,
            )
        },
        text = {
            Text(
                text = String.format(
                    stringResource(id = com.kape.ui.R.string.dip_remove_description),
                    server.name,
                    server.dedicatedIp,
                ),
                fontSize = 14.sp,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    server.dedicatedIp?.let(onRemoveClicked)
                    showDialog.value = false
                },
                modifier = Modifier
                    .testTag(":DedicatedIPScreen:dip_remove_confirm_button"),
            ) {
                Text(
                    text = stringResource(id = android.R.string.ok),
                    fontSize = 14.sp,
                    color = LocalColors.current.primary,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                    onCancelClicked()
                },
                modifier = Modifier
                    .testTag(":DedicatedIPScreen:dip_remove_cancel_button"),
            ) {
                Text(
                    text = stringResource(id = android.R.string.cancel),
                    fontSize = 14.sp,
                    color = LocalColors.current.primary,
                )
            }
        },
    )
}

@Composable
fun DedicatedIpSignupBanner(onAcceptTapped: () -> Unit) {
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        border = BorderStroke(1.dp, color = LocalColors.current.onPrimaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = CenterVertically,
            ) {
                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(
                        id = com.kape.ui.R.drawable.dedicated_ip,
                    ),
                    contentScale = ContentScale.Fit,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.weight(1.0f))
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                text = stringResource(id = com.kape.ui.R.string.dip_signup_banner_activate_description),
                color = LocalColors.current.onSurface,
                style = PiaTypography.subtitle2,
                textAlign = TextAlign.Start,
            )
            DedicatedIpSignupBenefitItem(
                text = stringResource(
                    id = com.kape.ui.R.string.dip_signup_banner_activate_benefit_one,
                ),
            )
            DedicatedIpSignupBenefitItem(
                text = stringResource(
                    id = com.kape.ui.R.string.dip_signup_banner_activate_benefit_two,
                ),
            )
            DedicatedIpSignupBenefitItem(
                text = stringResource(
                    id = com.kape.ui.R.string.dip_signup_banner_activate_benefit_three,
                ),
            )
            SecondaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = stringResource(id = com.kape.ui.R.string.get_dedicated_ip),
                onClick = onAcceptTapped,
            )
        }
    }
}

@Composable
fun DedicatedIpSignupBenefitItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = CenterVertically,
    ) {
        Image(
            modifier = Modifier.size(16.dp),
            painter = painterResource(
                id = com.kape.ui.R.drawable.checkmark,
            ),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            text = text,
            color = LocalColors.current.onSurface,
            style = PiaTypography.body1,
            textAlign = TextAlign.Start,
        )
    }
}