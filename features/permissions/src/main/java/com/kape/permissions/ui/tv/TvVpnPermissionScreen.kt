package com.kape.permissions.ui.tv

import android.net.VpnService
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kape.permissions.ui.mobile.getVpnProfileToastText
import com.kape.permissions.ui.vm.PermissionsViewModel
import com.kape.permissions.utils.GRANTED
import com.kape.permissions.utils.IDLE
import com.kape.permissions.utils.NOT_GRANTED
import com.kape.permissions.utils.REQUEST
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.tv.elements.PrimaryButton
import com.kape.ui.tv.text.OnboardingDescriptionText
import com.kape.ui.tv.text.OnboardingFooterText
import com.kape.ui.tv.text.OnboardingTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun TvVpnPermissionScreen() = Screen {
    val viewModel: PermissionsViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.vpnPermissionState }.collectAsState()
    val initialFocusRequester = FocusRequester()
    val startForResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.onVpnProfileStateChange()
        }

    when (state) {
        IDLE -> {
            ShowTvVpnPermissionScreen(
                viewModel = viewModel,
                initialFocusRequester = initialFocusRequester,
            )
        }
        REQUEST -> {
            ShowTvVpnPermissionScreen(
                viewModel = viewModel,
                initialFocusRequester = initialFocusRequester,
            )
            val intent = VpnService.prepare(LocalContext.current)
            startForResult.launch(intent)
        }
        GRANTED -> {
            // Do nothing. The viewmodel is handling the navigation on success.
        }
        NOT_GRANTED -> {
            ShowTvVpnPermissionScreen(
                viewModel = viewModel,
                initialFocusRequester = initialFocusRequester,
            )
            Toast.makeText(
                LocalContext.current,
                getVpnProfileToastText(granted = false),
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    LaunchedEffect(key1 = Unit) {
        initialFocusRequester.requestFocus()
    }
}

@Composable
fun ShowTvVpnPermissionScreen(
    viewModel: PermissionsViewModel,
    initialFocusRequester: FocusRequester,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalColors.current.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(64.dp),
        ) {
            Column(modifier = Modifier.semantics(mergeDescendants = true) { }) {
                OnboardingTitleText(
                    content = stringResource(id = R.string.vpn_permission_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
                OnboardingDescriptionText(
                    content = stringResource(id = R.string.vpn_permission_description),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
                OnboardingFooterText(
                    content = stringResource(id = R.string.vpn_permission_footer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }
            PrimaryButton(
                text = stringResource(id = R.string.ok),
                modifier = Modifier
                    .focusRequester(initialFocusRequester)
                    .padding(16.dp),
            ) {
                viewModel.onOkButtonClicked()
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(id = com.kape.permissions.R.drawable.tv_configuration),
                contentScale = ContentScale.Fit,
                contentDescription = null,
            )
        }
    }
}