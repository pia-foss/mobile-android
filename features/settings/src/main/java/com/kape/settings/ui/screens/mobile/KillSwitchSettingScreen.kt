package com.kape.settings.ui.screens.mobile

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.kape.ui.mobile.elements.AdaptiveSplitContent
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun KillSwitchSettingScreen() =
    Screen {
        val viewModel: SettingsViewModel = koinViewModel()
        val appBarViewModel: AppBarViewModel =
            koinViewModel<AppBarViewModel>().apply {
                appBarText(stringResource(id = R.string.privacy_kill_switch_title))
            }
        val context = LocalContext.current

        Scaffold(
            topBar = {
                AppBar(viewModel = appBarViewModel)
            },
        ) {
            val openVpnSettings = {
                context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))

                // There is no way for us to know if the settings have changed
                // We always show the dialog
                viewModel.showReconnectDialogIfVpnConnected()
            }

            AdaptiveSplitContent(
                modifier = Modifier.padding(it),
                header = { isCompact ->
                    if (isCompact) {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                    KillSwitchIllustration()
                    Spacer(modifier = Modifier.height(if (isCompact) 48.dp else 24.dp))
                    KillSwitchTitle()
                },
                body = {
                    Spacer(modifier = Modifier.height(16.dp))
                    KillSwitchDescription()
                    Spacer(modifier = Modifier.weight(1f))
                    KillSwitchAction(onClick = openVpnSettings)
                },
            )
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

@Composable
private fun KillSwitchIllustration() {
    Image(
        painter = painterResource(id = com.kape.settings.R.drawable.ic_vpn_permission),
        contentDescription = null,
        modifier = Modifier.width(80.dp),
    )
}

@Composable
private fun KillSwitchTitle() {
    Text(
        text = stringResource(id = R.string.kill_switch_title),
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun KillSwitchDescription() {
    Text(
        text = stringResource(id = R.string.kill_switch_description),
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun KillSwitchAction(onClick: () -> Unit) {
    PrimaryButton(
        text = stringResource(id = R.string.kill_switch_action),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        onClick = onClick,
    )
}