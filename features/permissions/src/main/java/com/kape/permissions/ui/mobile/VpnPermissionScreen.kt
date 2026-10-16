package com.kape.permissions.ui.mobile

import android.net.VpnService
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.permissions.R
import com.kape.permissions.ui.vm.PermissionsViewModel
import com.kape.permissions.utils.GRANTED
import com.kape.permissions.utils.IDLE
import com.kape.permissions.utils.NOT_GRANTED
import com.kape.permissions.utils.REQUEST
import com.kape.ui.mobile.elements.Screen
import org.koin.androidx.compose.koinViewModel

@Composable
fun VpnPermissionScreen() =
    Screen {
        val viewModel: PermissionsViewModel = koinViewModel()
        val state by remember(viewModel) { viewModel.vpnPermissionState }.collectAsStateWithLifecycle()
        val startForResult =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                viewModel.onVpnProfileStateChange()
            }

        when (state) {
            IDLE -> {}
            REQUEST -> {
                val intent = VpnService.prepare(LocalContext.current)
                if (intent != null) {
                    startForResult.launch(intent)
                } else {
                    viewModel.onVpnProfileStateChange()
                }
            }

            GRANTED -> {}
            NOT_GRANTED ->
                Toast
                    .makeText(
                        LocalContext.current,
                        getVpnProfileToastText(granted = false),
                        Toast.LENGTH_LONG,
                    ).show()
        }

        PermissionContent(
            illustration = R.drawable.image_lock,
            title = stringResource(id = com.kape.ui.R.string.vpn_permission_title),
            description = stringResource(id = com.kape.ui.R.string.vpn_permission_description),
            footer = stringResource(id = com.kape.ui.R.string.vpn_permission_footer),
            action = stringResource(id = com.kape.ui.R.string.ok),
            actionTestTag = ":VpnPermissionScreen:ok",
            onActionClick = viewModel::onOkButtonClicked,
        )
    }

fun getVpnProfileToastText(granted: Boolean): Int =
    if (granted) com.kape.ui.R.string.toast_vpn_profile_granted else com.kape.ui.R.string.toast_vpn_profile_not_granted