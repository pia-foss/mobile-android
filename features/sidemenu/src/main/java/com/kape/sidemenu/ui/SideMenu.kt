package com.kape.sidemenu.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kape.sidemenu.R
import com.kape.sidemenu.ui.vm.SideMenuViewModel
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.mobile.text.MenuText
import com.kape.ui.mobile.text.SideMenuUsernameText
import com.kape.ui.mobile.text.SideMenuVersionText
import com.kape.ui.theme.PiaScreen
import com.kape.ui.utils.LocalColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SideMenu(scope: CoroutineScope, drawerState: DrawerState, content: @Composable () -> Unit) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SideMenuContent(scope = scope, state = drawerState)
        },
    ) {
        content()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SideMenuContent(scope: CoroutineScope, state: DrawerState) {
    val viewModel: SideMenuViewModel = koinViewModel()
    val logoutDialogVisible = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(color = LocalColors.current.surface)
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .width(250.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .semantics {
                testTagsAsResourceId = true
            },
    ) {
        SideMenuHeaderItem(
            username = viewModel.username.value,
            versionCode = viewModel.versionCode.toString(),
            versionName = viewModel.versionName,
        )

        Spacer(modifier = Modifier.height(32.dp))

        SideMenuItem(
            iconId = R.drawable.ic_account,
            titleId = com.kape.ui.R.string.drawer_item_title_account,
            ":SideMenu:Account",
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToProfile()
        }

        SideMenuItem(
            iconId = R.drawable.ic_ip,
            titleId = com.kape.ui.R.string.drawer_item_title_dedicated_ip,
            ":SideMenu:DedicatedIp",
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToDedicatedIp()
        }

        SideMenuItem(
            iconId = R.drawable.ic_per_app,
            titleId = com.kape.ui.R.string.drawer_item_title_per_app_settings,
            ":SideMenu:PerAppSettings",
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToPerAppSettings()
        }

        SideMenuItem(
            iconId = com.kape.ui.R.drawable.ic_settings,
            titleId = com.kape.ui.R.string.drawer_item_title_settings,
            ":SideMenu:Settings",
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToSettings()
        }

        SideMenuItem(
            iconId = R.drawable.ic_log_out,
            titleId = com.kape.ui.R.string.drawer_item_title_logout,
            ":SideMenu:Logout",
        ) {
            scope.launch {
                state.close()
            }
            logoutDialogVisible.value = true
        }
        Spacer(modifier = Modifier.height(16.dp))
        Separator()
        Spacer(modifier = Modifier.height(16.dp))
        SideMenuItem(
            iconId = R.drawable.ic_about,
            titleId = com.kape.ui.R.string.drawer_item_title_about,
            ":SideMenu:About",
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToAbout()
        }
        SideMenuItem(
            iconId = R.drawable.ic_privacy,
            titleId = com.kape.ui.R.string.drawer_item_title_privacy_policy,
            ":SideMenu:PrivacyPolicy",
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToPrivacyPolicy()
        }

        SideMenuItem(
            iconId = com.kape.ui.R.drawable.ic_help,
            titleId = com.kape.ui.R.string.drawer_item_title_contact_support,
            ":SideMenu:ContactSupport",
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToSupport()
        }
    }

    if (logoutDialogVisible.value) {
        LogoutDialog(
            onDismiss = {
                logoutDialogVisible.value = false
            },
            onConfirm = {
                viewModel.logout()
                logoutDialogVisible.value = false
            },
        )
    }
}

@Composable
private fun SideMenuHeaderItem(username: String, versionCode: String, versionName: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.drawer_icon),
            modifier = Modifier
                .align(CenterVertically)
                .size(48.dp),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(CenterVertically),
        ) {
            val usernamePrefix = stringResource(id = com.kape.ui.R.string.username)
            SideMenuUsernameText(
                content = username,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "$usernamePrefix $username"
                    },
            )
            Spacer(modifier = Modifier.height(4.dp))
            SideMenuVersionText(
                content = stringResource(com.kape.ui.R.string.drawer_item_description_app_version_format).format(
                    versionName,
                    versionCode,
                ),
                modifier = Modifier.wrapContentWidth(),
            )
        }
    }
}

@Composable
private fun SideMenuItem(
    @DrawableRes iconId: Int,
    @StringRes titleId: Int,
    testTag: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable {
                onClick.invoke()
            },
    ) {
        Image(
            modifier = Modifier
                .align(CenterVertically)
                .size(24.dp)
                .testTag(testTag),
            painter = painterResource(id = iconId),
            contentScale = ContentScale.Inside,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.width(16.dp))

        MenuText(
            content = stringResource(id = titleId),
            modifier = Modifier.align(CenterVertically),
        )
    }
}

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = com.kape.ui.R.string.drawer_item_title_logout))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = com.kape.ui.R.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(id = com.kape.ui.R.string.logout_confirmation),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = CenterVertically,
                ) {
                    Text(text = stringResource(id = com.kape.ui.R.string.logout_confirmation_text))
                }
            }
        },
    )
}

@Preview
@Composable
fun PreviewHeader() {
    PiaScreen {
        SideMenuHeaderItem(username = "p2860501", versionCode = "123", versionName = "4.0")
    }
}