package com.kape.vpn_permissions.ui

import android.net.VpnService
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kape.uicomponents.components.ButtonProperties
import com.kape.uicomponents.components.PrimaryButton
import com.kape.uicomponents.components.UiResources
import com.kape.uicomponents.theme.Space
import com.kape.uicomponents.theme.Typography
import com.kape.vpn_permissions.R
import com.kape.vpn_permissions.ui.vm.PermissionsViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun VpnSystemProfileScreen() {

    val viewModel: PermissionsViewModel = getViewModel()
    val state by remember(viewModel) { viewModel.showState }.collectAsState()
    val startForResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.onVpnProfileStateChange()
        }
    val okButtonProperties = ButtonProperties(
        label = stringResource(id = R.string.setup_vpn_profile_ok_button),
        enabled = true,
        onClick = {
            viewModel.onOkButtonClicked()
        }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = UiResources.bigAppLogo),
            contentDescription = "logo",
            modifier = Modifier.padding(start = Space.CENT, top = Space.BIGGER, bottom = Space.HUGE, end = Space.CENT)
        )
        Text(
            text = stringResource(id = R.string.setup_vpn_profile_description),
            style = Typography.subtitle1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(Space.HUGE)
        )
        PrimaryButton(
            modifier = Modifier.padding(Space.MEDIUM, Space.MINI),
            properties = okButtonProperties
        )
        Text(
            text = stringResource(id = R.string.setup_vpn_profile_privacy_statement),
            style = Typography.subtitle1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(Space.HUGE)
        )

        when (state) {
            PermissionsViewModel.ShowState.IDLE -> viewModel.onScreenLaunch()
            PermissionsViewModel.ShowState.SHOW_VPN_PROFILE_NOT_GRANTED_TOAST -> {
                Toast.makeText(LocalContext.current, getVpnProfileToastText(granted = false), Toast.LENGTH_LONG).show()
            }
            PermissionsViewModel.ShowState.REQUEST_VPN_PROFILE -> {
                val intent = VpnService.prepare(LocalContext.current) ?: return
                startForResult.launch(intent)
            }
        }
    }
}

fun getVpnProfileToastText(granted: Boolean): Int {
    return if (granted) R.string.toast_vpn_profile_granted else R.string.toast_vpn_profile_not_granted
}

@Preview
@Composable
fun ShowVpnSystemProfileScreen() {
    VpnSystemProfileScreen()
}
