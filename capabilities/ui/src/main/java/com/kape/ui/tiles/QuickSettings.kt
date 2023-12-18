package com.kape.ui.tiles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.text.QuickConnectText
import com.kape.ui.text.TileTitleText
import com.kape.ui.utils.LocalColors

@Composable
fun QuickSettings(
    onKillSwitchClick: () -> Unit,
    onAutomationClick: () -> Unit,
    onProtocolsClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
    ) {
        TileTitleText(content = stringResource(id = R.string.quick_settings))
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            QuickSettingItem(
                iconId = R.drawable.ic_quick_automation,
                labelId = R.string.automation,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onAutomationClick()
                    },
            )

            QuickSettingItem(
                iconId = R.drawable.ic_quick_killswitch,
                labelId = R.string.vpn_kill_switch,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onKillSwitchClick()
                    },
            )

            QuickSettingItem(
                iconId = R.drawable.ic_quick_protocols,
                labelId = R.string.protocols,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProtocolsClick() },
            )
        }
    }
}

@Composable
private fun QuickSettingItem(iconId: Int, labelId: Int, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(LocalColors.current.onPrimary, CircleShape)
                .padding(4.dp),
        ) {
            Icon(
                painter = painterResource(
                    id = iconId,
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                tint = LocalColors.current.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        QuickConnectText(
            content = stringResource(id = labelId),
            modifier = Modifier.align(CenterHorizontally),
        )
    }
}