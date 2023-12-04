package com.kape.connection.ui.tiles

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
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.connection.R
import com.kape.connection.utils.SnoozeInterval
import com.kape.ui.text.QuickConnectText
import com.kape.ui.text.TileTitleText
import com.kape.ui.utils.LocalColors

@Composable
fun Snooze(
    active: MutableState<Boolean>,
    onClick: (interval: SnoozeInterval) -> Unit,
    onResumeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
    ) {
        TileTitleText(content = stringResource(id = R.string.vpn_snooze))
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            if (active.value) {
                SnoozeItem(
                    iconId = R.drawable.ic_snooze_resume,
                    labelId = R.string.resume,
                    isActive = true,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onResumeClick()
                        },
                )
            } else {
                SnoozeItem(
                    iconId = R.drawable.ic_snooze,
                    labelId = R.string.snooze_5_minutes,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onClick(SnoozeInterval.SNOOZE_SHORT_MS)
                        },
                )

                SnoozeItem(
                    iconId = R.drawable.ic_snooze,
                    labelId = R.string.snooze_15_minutes,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onClick(SnoozeInterval.SNOOZE_MEDIUM_MS)
                        },
                )

                SnoozeItem(
                    iconId = R.drawable.ic_snooze,
                    labelId = R.string.snooze_1_hour,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onClick(SnoozeInterval.SNOOZE_LONG_MS)
                        },
                )
            }
        }
    }
}

@Composable
private fun SnoozeItem(iconId: Int, labelId: Int, isActive: Boolean = false, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
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
                tint = if (isActive) LocalColors.current.primary else LocalColors.current.onSurface,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        QuickConnectText(
            content = stringResource(id = labelId),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}