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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kape.sidemenu.R
import com.kape.sidemenu.ui.vm.SideMenuViewModel
import com.kape.ui.elements.Separator
import com.kape.ui.text.MenuText
import com.kape.ui.text.SideMenuUsernameText
import com.kape.ui.text.SideMenuVersionText
import com.kape.ui.theme.PiaScreen
import com.kape.ui.utils.LocalColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

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

@Composable
private fun SideMenuContent(scope: CoroutineScope, state: DrawerState) {
    val viewModel: SideMenuViewModel = getViewModel()

    Column(
        modifier = Modifier
            .background(color = LocalColors.current.surface)
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .width(300.dp)
            .fillMaxHeight(),
    ) {
        SideMenuHeaderItem(
            username = viewModel.username.value,
            versionCode = viewModel.versionCode.toString(),
            versionName = viewModel.versionName,
        )

        Spacer(modifier = Modifier.height(32.dp))

        SideMenuItem(iconId = R.drawable.ic_account, titleId = R.string.drawer_item_title_account) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToProfile()
        }

        SideMenuItem(iconId = R.drawable.ic_ip, titleId = R.string.drawer_item_title_dedicated_ip) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToDedicatedIp()
        }

        SideMenuItem(
            iconId = R.drawable.ic_per_app,
            titleId = R.string.drawer_item_title_per_app_settings,
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToPerAppSettings()
        }

        SideMenuItem(
            iconId = R.drawable.ic_settings,
            titleId = R.string.drawer_item_title_settings,
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToSettings()
        }

        SideMenuItem(iconId = R.drawable.ic_log_out, titleId = R.string.drawer_item_title_logout) {
            scope.launch {
                state.close()
            }
            viewModel.logout()
        }

        Separator()

        SideMenuItem(iconId = R.drawable.ic_about, titleId = R.string.drawer_item_title_about) {
            scope.launch {
                state.close()
            }
            // todo: implement once about feature is ready
        }
        SideMenuItem(
            iconId = R.drawable.ic_privacy,
            titleId = R.string.drawer_item_title_privacy_policy,
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToPrivacyPolicy()
        }

        SideMenuItem(
            iconId = R.drawable.ic_help,
            titleId = R.string.drawer_item_title_contact_support,
        ) {
            scope.launch {
                state.close()
            }
            viewModel.navigateToSupport()
        }
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
            SideMenuUsernameText(content = username, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            SideMenuVersionText(
                content = stringResource(R.string.drawer_item_description_app_version_format).format(
                    versionName,
                    versionCode,
                ),
                modifier = Modifier.wrapContentWidth(),
            )
        }
    }
}

@Composable
private fun SideMenuItem(@DrawableRes iconId: Int, @StringRes titleId: Int, onClick: () -> Unit) {
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
                .size(24.dp),
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

@Preview
@Composable
fun PreviewHeader() {
    PiaScreen {
        SideMenuHeaderItem(username = "p2860501", versionCode = "123", versionName = "4.0")
    }
}