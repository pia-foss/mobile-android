package com.kape.automation.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.automation.R
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.networkmanagement.data.NetworkBehavior
import com.kape.networkmanagement.data.NetworkItem
import com.kape.networkmanagement.data.NetworkType
import com.kape.ui.elements.NetworkCard
import com.kape.ui.elements.Screen
import com.kape.ui.text.DialogActionText
import com.kape.ui.text.DialogTitleText
import com.kape.ui.text.Hyperlink
import com.kape.ui.text.OnboardingDescriptionText
import com.kape.ui.text.OnboardingTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun AutomationScreen() = Screen {
    val viewModel: AutomationViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.trusted_network_plural))
    }
    val showDialog = remember { mutableStateOf(false) }
    var currentItem = remember { mutableStateOf<NetworkItem?>(null) }

    val context: Context = LocalContext.current

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
            ) {
                viewModel.navigateUp()
            }
        },
    ) {
        Column(modifier = Modifier.padding(it)) {
            OnboardingTitleText(
                content = stringResource(id = R.string.manage_automation_title),
                modifier = Modifier.padding(16.dp),
            )

            OnboardingDescriptionText(
                content = stringResource(id = R.string.manage_automation_description),
                modifier = Modifier.padding(16.dp),
            )

            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                items(viewModel.getNetworkItems()) { networkItem ->
                    val icon: Int
                    val title: String
                    val status = getStatus(behavior = networkItem.networkBehavior)

                    when (networkItem.networkType) {
                        NetworkType.MobileData -> {
                            icon = com.kape.ui.R.drawable.ic_mobile_data
                            title =
                                stringResource(id = com.kape.networkmanagement.R.string.nmt_mobile_data)
                        }

                        NetworkType.WifiCustom -> {
                            icon = com.kape.ui.R.drawable.ic_wifi_custom
                            title =
                                stringResource(id = com.kape.networkmanagement.R.string.nmt_open_wifi)
                        }

                        NetworkType.WifiOpen -> {
                            icon = com.kape.ui.R.drawable.ic_open_wifi
                            title = networkItem.networkName
                        }

                        NetworkType.WifiSecure -> {
                            icon = com.kape.ui.R.drawable.ic_secure_wifi
                            title =
                                stringResource(id = com.kape.networkmanagement.R.string.nmt_secure_wifi)
                        }
                    }
                    NetworkCard(
                        icon = icon,
                        title = title,
                        status = status,
                        isDefault = networkItem.isDefault,
                    ) {
                        currentItem.value = networkItem
                        showDialog.value = true
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .clickable {
                        viewModel.navigateToAddNewRule()
                    },
            ) {
                Icon(
                    painter = painterResource(id = com.kape.ui.R.drawable.ic_add),
                    contentDescription = null,
                    tint = LocalColors.current.primary,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Hyperlink(
                    content = stringResource(id = R.string.manage_automation_add),
                    modifier = Modifier.align(CenterVertically),
                )
            }
        }
    }

    if (showDialog.value) {
        currentItem.value?.let {
            BehaviorDialog(status = getStatus(behavior = it.networkBehavior), showDialog) {
                currentItem.value?.let { item ->
                    viewModel.updateRule(item, getRuleForStatus(context, status = it))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorDialog(
    status: String,
    showDialog: MutableState<Boolean>,
    onItemSelected: (selected: String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        modifier = Modifier.background(LocalColors.current.surfaceVariant),
    ) {
        val radioOptions = listOf(
            stringResource(id = com.kape.networkmanagement.R.string.nmt_connect),
            stringResource(id = com.kape.networkmanagement.R.string.nmt_disconnect),
            stringResource(id = com.kape.networkmanagement.R.string.nmt_retain),
        )
        val selectedOption = remember {
            mutableStateOf(
                radioOptions[
                    radioOptions.indexOf(
                        status,
                    ),
                ],
            )
        }
        Column(
            Modifier.fillMaxWidth(),
        ) {
            DialogTitleText(
                content = stringResource(id = R.string.dialog_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        LocalColors.current.surface,
                    )
                    .padding(16.dp),
            )
            radioOptions.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (text == selectedOption.value),
                            onClick = {
                                selectedOption.value = text
                            },
                        )
                        .padding(vertical = 4.dp),
                ) {
                    RadioButton(
                        selected = (text == selectedOption.value),
                        onClick = {
                            selectedOption.value = text
                        },
                    )
                    Text(
                        text = text,
                        modifier = Modifier.align(CenterVertically),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(End)
                    .padding(16.dp),
            ) {
                DialogActionText(
                    content = stringResource(id = android.R.string.cancel),
                    modifier = Modifier.clickable {
                        showDialog.value = false
                    },
                )
                Spacer(modifier = Modifier.width(16.dp))
                DialogActionText(
                    content = stringResource(id = android.R.string.ok),
                    modifier = Modifier.clickable {
                        onItemSelected(selectedOption.value)
                        showDialog.value = false
                    },
                )
            }
        }
    }
}

@Composable
private fun getStatus(behavior: NetworkBehavior): String {
    return when (behavior) {
        NetworkBehavior.AlwaysConnect -> stringResource(id = com.kape.networkmanagement.R.string.nmt_connect)
        NetworkBehavior.AlwaysDisconnect -> stringResource(id = com.kape.networkmanagement.R.string.nmt_disconnect)
        NetworkBehavior.RetainState -> stringResource(id = com.kape.networkmanagement.R.string.nmt_retain)
    }
}

private fun getRuleForStatus(context: Context, status: String): NetworkBehavior {
    return when (status) {
        context.getString(com.kape.networkmanagement.R.string.nmt_retain) -> NetworkBehavior.RetainState
        context.getString(com.kape.networkmanagement.R.string.nmt_disconnect) -> NetworkBehavior.AlwaysDisconnect
        else -> NetworkBehavior.AlwaysConnect
    }
}