package com.kape.sidemenu.ui.screens.mobile

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kape.sidemenu.R
import com.kape.sidemenu.ui.vm.SideMenuViewModel
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.mobile.text.MenuText
import com.kape.ui.tiles.LogoutDialog
import org.koin.androidx.compose.koinViewModel

/**
 * Overflow for the navigation rail: the side menu entries that are not top-level
 * destinations (account header with version / update link, logout, about, privacy
 * policy and contact support), plus any [destinations] that don't fit on the rail.
 * Anchors to its parent, so place it in a Box next to the rail item's icon.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SideMenuMoreMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    destinations: List<MoreMenuDestination> = emptyList(),
) {
    val viewModel: SideMenuViewModel = koinViewModel()
    val hasUpdateAvailable by viewModel.updateAvailable.collectAsStateWithLifecycle()
    var logoutDialogVisible by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(expanded) {
        if (expanded) viewModel.refreshProfile()
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.semantics { testTagsAsResourceId = true },
    ) {
        Box(
            modifier =
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .width(250.dp),
        ) {
            SideMenuHeaderItem(
                username = viewModel.username.value,
                versionCode = viewModel.getVersionCode(),
                versionName = viewModel.getVersionName(),
                hasUpdateAvailable = hasUpdateAvailable,
                onClick = {
                    if (hasUpdateAvailable) {
                        onDismiss()
                        openUpdateDownload(context, viewModel.getDownloadLink())
                    }
                },
            )
        }

        Separator()

        if (destinations.isNotEmpty()) {
            destinations.forEach { destination ->
                MoreMenuItem(
                    iconId = destination.iconId,
                    titleId = destination.titleId,
                    testTag = destination.testTag,
                ) {
                    onDismiss()
                    destination.onClick()
                }
            }
            Separator()
        }

        MoreMenuItem(
            iconId = R.drawable.ic_log_out,
            titleId = com.kape.ui.R.string.drawer_item_title_logout,
            testTag = ":MoreMenu:Logout",
        ) {
            onDismiss()
            logoutDialogVisible = true
        }

        Separator()

        MoreMenuItem(
            iconId = R.drawable.ic_about,
            titleId = com.kape.ui.R.string.drawer_item_title_about,
            testTag = ":MoreMenu:About",
        ) {
            onDismiss()
            viewModel.navigateToAbout()
        }
        MoreMenuItem(
            iconId = R.drawable.ic_privacy,
            titleId = com.kape.ui.R.string.drawer_item_title_privacy_policy,
            testTag = ":MoreMenu:PrivacyPolicy",
        ) {
            onDismiss()
            viewModel.navigateToPrivacyPolicy()
        }
        MoreMenuItem(
            iconId = com.kape.ui.R.drawable.ic_help,
            titleId = com.kape.ui.R.string.drawer_item_title_contact_support,
            testTag = ":MoreMenu:ContactSupport",
        ) {
            onDismiss()
            viewModel.navigateToSupport()
        }
    }

    if (logoutDialogVisible) {
        LogoutDialog(
            onDismiss = { logoutDialogVisible = false },
            onConfirm = {
                viewModel.logout()
                logoutDialogVisible = false
            },
        )
    }
}

/** A navigation destination shown in the More menu when it doesn't fit on the rail. */
class MoreMenuDestination(
    @DrawableRes val iconId: Int,
    @StringRes val titleId: Int,
    val testTag: String,
    val onClick: () -> Unit,
)

@Composable
private fun MoreMenuItem(
    @DrawableRes iconId: Int,
    @StringRes titleId: Int,
    testTag: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { MenuText(content = stringResource(id = titleId), modifier = Modifier) },
        leadingIcon = {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        },
        onClick = onClick,
        modifier = Modifier.testTag(testTag),
    )
}