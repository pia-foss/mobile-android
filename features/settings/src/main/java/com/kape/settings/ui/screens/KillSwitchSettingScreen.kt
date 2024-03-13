package com.kape.settings.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.settings.ui.elements.ReconnectDialog
import com.kape.settings.ui.vm.SettingsViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun KillSwitchSettingScreen() = Screen {
    val viewModel: SettingsViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.privacy_kill_switch_title))
    }
    val context = LocalContext.current

    BackHandler {
        viewModel.navigateUp()
    }

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                onLeftIconClick = { viewModel.navigateUp() },
            )
        },
    ) {
        Column(
            Modifier
                .padding(it)
                .fillMaxSize()
                .background(LocalColors.current.background),
            horizontalAlignment = CenterHorizontally,
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                Spacer(modifier = Modifier.height(48.dp))
                Image(
                    painter = painterResource(id = com.kape.settings.R.drawable.ic_vpn_permission),
                    contentDescription = null,
                    modifier = Modifier
                        .align(CenterHorizontally)
                        .width(80.dp),
                )
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = stringResource(id = R.string.kill_switch_title),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(CenterHorizontally),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.kill_switch_description),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(CenterHorizontally),
                )
                Spacer(modifier = Modifier.weight(1f))
                PrimaryButton(
                    text = stringResource(id = R.string.kill_switch_action),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))

                    // There is no way for us to know if the settings have changed
                    // We always show the dialog
                    viewModel.showReconnectDialogIfVpnConnected()
                }
            }
        }
        if (viewModel.reconnectDialogVisible.value) {
            ReconnectDialog(
                onReconnect = {
                    viewModel.reconnect()
                    viewModel.reconnectDialogVisible.value = false
                },
                onLater = {
                    viewModel.reconnectDialogVisible.value = false
                },
            )
        }
    }
}