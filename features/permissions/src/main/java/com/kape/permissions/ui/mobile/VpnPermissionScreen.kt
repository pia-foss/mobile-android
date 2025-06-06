package com.kape.permissions.ui.mobile

import android.net.VpnService
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.kape.permissions.R
import com.kape.permissions.ui.vm.PermissionsViewModel
import com.kape.permissions.utils.GRANTED
import com.kape.permissions.utils.IDLE
import com.kape.permissions.utils.NOT_GRANTED
import com.kape.permissions.utils.REQUEST
import com.kape.ui.mobile.elements.PrimaryButton
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.text.OnboardingDescriptionText
import com.kape.ui.mobile.text.OnboardingFooterText
import com.kape.ui.mobile.text.OnboardingTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VpnPermissionScreen() = Screen {
    val viewModel: PermissionsViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.vpnPermissionState }.collectAsState()
    val startForResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.onVpnProfileStateChange()
        }

    when (state) {
        IDLE -> {}
        REQUEST -> {
            val intent = VpnService.prepare(LocalContext.current)
            startForResult.launch(intent)
        }

        GRANTED -> {}
        NOT_GRANTED -> Toast.makeText(
            LocalContext.current,
            getVpnProfileToastText(granted = false),
            Toast.LENGTH_LONG,
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(LocalColors.current.background)
            .semantics {
                testTagsAsResourceId = true
            },
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = CenterHorizontally,
    ) {
        Column(modifier = Modifier.widthIn(max = 520.dp)) {
            Image(
                painter = painterResource(id = com.kape.ui.R.drawable.pia_medium),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .height(40.dp)
                    .fillMaxWidth(),
            )
            Image(
                painter = painterResource(id = R.drawable.image_lock),
                contentDescription = null,
                modifier = Modifier
                    .padding(40.dp)
                    .height(140.dp)
                    .fillMaxWidth(),
            )
            Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
                OnboardingTitleText(
                    content = stringResource(id = com.kape.ui.R.string.vpn_permission_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
                OnboardingDescriptionText(
                    content = stringResource(id = com.kape.ui.R.string.vpn_permission_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                OnboardingFooterText(
                    content = stringResource(id = com.kape.ui.R.string.vpn_permission_footer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }
            PrimaryButton(
                text = stringResource(id = com.kape.ui.R.string.ok),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, bottom = 36.dp, end = 16.dp)
                    .align(CenterHorizontally)
                    .testTag(":VpnPermissionScreen:ok"),
            ) {
                viewModel.onOkButtonClicked()
            }
        }
    }
}

fun getVpnProfileToastText(granted: Boolean): Int {
    return if (granted) com.kape.ui.R.string.toast_vpn_profile_granted else com.kape.ui.R.string.toast_vpn_profile_not_granted
}