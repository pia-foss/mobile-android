package com.kape.automation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.automation.R
import com.kape.automation.ui.viewmodel.AutomationViewModel
import com.kape.ui.elements.Screen
import com.kape.ui.text.OnboardingFooterText
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddNewRuleScreen() = Screen {
    val viewModel: AutomationViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.trusted_network_plural))
    }
    val showDialog = remember { mutableStateOf(false) }

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
        }
    }
}