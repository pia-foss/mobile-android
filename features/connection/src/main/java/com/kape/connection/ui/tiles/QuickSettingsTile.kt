package com.kape.connection.ui.tiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.ui.elements.ConnectionTile
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.Space
import com.kape.ui.theme.Square
import com.kape.ui.theme.Width
import com.kape.ui.utils.LocalColors

@Composable
fun QuickSettingsTile(
    isKillSwitchEnabled: Boolean,
    isAutomationEnabled: Boolean,
    isPrivateBrowserEnabled: Boolean,
    onKillSwitchClick: () -> Unit,
    onAutomationClick: () -> Unit,
    onPrivateBrowserClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        ConnectionTile(labelId = R.string.quick_settings) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isKillSwitchEnabled) {
                    QuickSetting(
                        iconId = com.kape.ui.R.drawable.ic_killswitch,
                        labelId = R.string.vpn_kill_switch,
                        modifier = Modifier.weight(1f),
                        onClick = onKillSwitchClick,
                    )
                }

                if (isAutomationEnabled) {
                    QuickSetting(
                        iconId = com.kape.ui.R.drawable.ic_network_management_inactive,
                        labelId = R.string.automation,
                        modifier = Modifier.weight(1f),
                        onClick = onAutomationClick,
                    )
                }

                if (isPrivateBrowserEnabled) {
                    QuickSetting(
                        iconId = com.kape.ui.R.drawable.ic_private_browser,
                        labelId = R.string.private_browser,
                        modifier = Modifier.weight(1f),
                        onClick = onPrivateBrowserClick,
                    )
                }
            }
        }

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier
                .align(CenterEnd)
                .padding(end = Space.NORMAL)
                .width(Width.QUICK_SETTINGS_ARROW)
                .height(Height.QUICK_SETTINGS_ARROW),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
fun QuickSetting(iconId: Int, labelId: Int, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clickable {
            onClick()
        },
        horizontalAlignment = CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .size(Square.QUICK_SETTING_ICON)
                .align(CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(Space.SMALL))
        Text(
            text = stringResource(id = labelId),
            color = LocalColors.current.onSurface,
            fontSize = FontSize.Small,
        )
    }
}