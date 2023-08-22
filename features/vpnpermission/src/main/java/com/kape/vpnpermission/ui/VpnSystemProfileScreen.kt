package com.kape.vpnpermission.ui

import android.net.VpnService
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.kape.ui.elements.ButtonProperties
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.UiResources
import com.kape.ui.theme.Space
import com.kape.vpnpermission.ui.vm.PermissionsViewModel
import com.kape.vpnpermission.utils.GRANTED
import com.kape.vpnpermission.utils.IDLE
import com.kape.vpnpermission.utils.NOT_GRANTED
import com.kape.vpnpermission.utils.REQUEST
import org.koin.androidx.compose.getViewModel

@Composable
fun VpnSystemProfileScreen() {
    val viewModel: PermissionsViewModel = getViewModel()
    val state by remember(viewModel) { viewModel.vpnPermissionState }.collectAsState()
    val startForResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.onVpnProfileStateChange()
        }
    val okButtonProperties =
        ButtonProperties(
            label = stringResource(id = com.kape.vpnpermission.R.string.setup_vpn_profile_ok_button),
            enabled = true,
            onClick = {
                viewModel.onOkButtonClicked()
            },
        )

    LaunchedEffect(key1 = state) {
        viewModel.checkFlowCompleted()
    }

    when (state) {
        IDLE -> {}
        REQUEST -> {
            val intent = VpnService.prepare(LocalContext.current) ?: return
            startForResult.launch(intent)
        }

        GRANTED -> {}
        NOT_GRANTED -> Toast.makeText(
            LocalContext.current,
            getVpnProfileToastText(granted = false),
            Toast.LENGTH_LONG,
        ).show()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = UiResources.bigAppLogo),
            contentDescription = "logo",
            modifier = Modifier.padding(
                start = Space.CENT,
                top = Space.BIGGER,
                bottom = Space.HUGE,
                end = Space.CENT,
            ),
        )
        Text(
            text = stringResource(id = com.kape.vpnpermission.R.string.setup_vpn_profile_description),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(Space.HUGE),
        )
        PrimaryButton(
            modifier = Modifier.padding(Space.MEDIUM, Space.MINI),
            properties = okButtonProperties,
        )
        Text(
            text = stringResource(id = com.kape.vpnpermission.R.string.setup_vpn_profile_privacy_statement),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(Space.HUGE),
        )
    }
}

fun getVpnProfileToastText(granted: Boolean): Int {
    return if (granted) com.kape.vpnpermission.R.string.toast_vpn_profile_granted else com.kape.vpnpermission.R.string.toast_vpn_profile_not_granted
}

@Preview
@Composable
fun ShowVpnSystemProfileScreen() {
    VpnSystemProfileScreen()
}