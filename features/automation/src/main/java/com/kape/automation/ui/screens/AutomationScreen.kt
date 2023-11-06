package com.kape.automation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.automation.R
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.ui.elements.NetworkCard
import com.kape.ui.elements.Screen
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

            Row(modifier = Modifier.padding(16.dp)) {
                NetworkCard(
                    icon = com.kape.ui.R.drawable.ic_open_wifi,
                    title = stringResource(id = R.string.nmt_open_wifi),
                    status = stringResource(id = R.string.nmt_connect),
                    modifier = Modifier
                        .weight(0.48f)
                        .clickable {
                            // TODO: implement click
                        },
                )
                Spacer(modifier = Modifier.width(16.dp))
                NetworkCard(
                    icon = com.kape.ui.R.drawable.ic_mobile_data,
                    title = stringResource(id = R.string.nmt_mobile_data),
                    status = stringResource(id = R.string.nmt_connect),
                    modifier = Modifier
                        .weight(0.48f)
                        .clickable {
                            // TODO: implement click
                        },
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.align(CenterHorizontally)) {
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
}