package com.kape.profile.ui.screens.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kape.appbar.view.mobile.AppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.profile.ui.vm.ProfileViewModel
import com.kape.ui.R
import com.kape.ui.mobile.elements.Screen
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.mobile.text.HyperlinkRed
import com.kape.ui.mobile.text.SettingsL2Text
import com.kape.ui.mobile.text.SettingsL2TextDescription
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen() = Screen {
    val viewModel: ProfileViewModel = koinViewModel()
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.account))
    }
    val state by remember(viewModel) { viewModel.screenState }.collectAsState()

    Column {
        AppBar(
            viewModel = appBarViewModel,
            onLeftIconClick = { viewModel.navigateBack() },
        )
        if (state.loading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .background(LocalColors.current.surfaceVariant)
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
                        SettingsL2Text(content = stringResource(id = R.string.username))
                        Text(
                            color = LocalColors.current.onSurfaceVariant,
                            text = state.username,
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    SettingsL2TextDescription(content = stringResource(id = R.string.message_other_devices))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
                        Text(
                            color = LocalColors.current.outlineVariant,
                            text = stringResource(id = if (state.expired) R.string.message_expired else R.string.message_expiration),
                            fontSize = 12.sp,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            color = LocalColors.current.onSurface,
                            text = if (state.expired) stringResource(id = R.string.subscription_status_expired) else state.expirationDate,
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Separator()
                Column(
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .background(LocalColors.current.surfaceVariant)
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    SettingsL2Text(content = stringResource(id = R.string.account_deletion_title))
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsL2TextDescription(content = stringResource(id = R.string.account_deletion_description))
                    Spacer(modifier = Modifier.height(8.dp))
                    HyperlinkRed(
                        content = stringResource(id = R.string.account_deletion_action),
                        modifier = Modifier.clickable {
                            viewModel.navigateToDeleteAccount()
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}