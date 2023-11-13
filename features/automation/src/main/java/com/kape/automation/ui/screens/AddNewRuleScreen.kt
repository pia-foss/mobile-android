package com.kape.automation.ui.screens

import android.content.Context
import android.net.wifi.ScanResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.automation.R
import com.kape.automation.ui.elements.BehaviorDialog
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.ui.elements.Screen
import com.kape.ui.text.MenuText
import com.kape.ui.text.OnboardingFooterText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddNewRuleScreen() = Screen {
    val viewModel: AutomationViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.trusted_network_plural))
    }
    val showDialog = remember { mutableStateOf(false) }
    val currentItem = remember { mutableStateOf<ScanResult?>(null) }
    val context: Context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.scanNetworks()
    }

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
            OnboardingFooterText(
                content = stringResource(id = R.string.add_rule_title),
                modifier = Modifier.padding(16.dp),
            )
            LazyColumn {
                items(viewModel.availableNetworks.value) {
                    WifiNetworkItem(scanResult = it, showDialog) {
                        currentItem.value = it
                        showDialog.value = true
                    }
                }
            }
        }

        if (showDialog.value) {
            BehaviorDialog(
                status = stringResource(id = R.string.nmt_connect),
                showRemoveOption = false,
                showDialog,
            ) {
                currentItem.value?.let { item ->
                    viewModel.addRule(item, getRuleForStatus(context, it))
                }
            }
        }
    }
}

@Composable
fun WifiNetworkItem(
    scanResult: ScanResult,
    showDialog: MutableState<Boolean>,
    onClick: (scanResult: ScanResult) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                showDialog.value = true
                onClick(scanResult)
            },
    ) {
        Icon(
            painter = painterResource(id = com.kape.ui.R.drawable.ic_open_wifi),
            contentDescription = null,
            tint = LocalColors.current.onSurface,
        )
        Spacer(modifier = Modifier.width(16.dp))
        MenuText(
            content = scanResult.SSID,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
    }
}