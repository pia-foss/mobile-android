package com.kape.automation.ui.screens

import android.Manifest
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.automation.R
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.ui.elements.PrimaryButton
import com.kape.ui.elements.Screen
import com.kape.ui.text.OnboardingDescriptionText
import com.kape.ui.text.OnboardingFooterText
import com.kape.ui.text.OnboardingTitleText
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun LocationPermissionScreen() = Screen {
    val viewModel: AutomationViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = com.kape.ui.R.string.trusted_network_plural))
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            if (it) {
                viewModel.navigateToNextScreen()
            }
        },
    )

    Scaffold(
        topBar = {
            AppBar(
                viewModel = appBarViewModel,
                onLeftIconClick = { viewModel.exitAutomation() },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
                .background(LocalColors.current.background),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
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
                    painter = painterResource(id = R.drawable.ic_vpn_permission),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(40.dp)
                        .height(140.dp)
                        .fillMaxWidth(),
                )
                OnboardingTitleText(
                    content = stringResource(id = com.kape.ui.R.string.location_permission_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )

                OnboardingDescriptionText(
                    content = stringResource(id = com.kape.ui.R.string.location_permission_message),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )

                Spacer(modifier = Modifier.weight(1f))

                OnboardingFooterText(
                    content = stringResource(id = com.kape.ui.R.string.location_permission_footer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )

                PrimaryButton(
                    text = stringResource(id = com.kape.ui.R.string.location_permission_action),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp, bottom = 36.dp, end = 16.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }
}