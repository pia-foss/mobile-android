package com.kape.connection.ui.tiles

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.connection.utils.SnoozeInterval
import com.kape.connection.utils.SnoozeState
import com.kape.ui.elements.ConnectionTile
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Space
import com.kape.ui.theme.Width
import com.kape.ui.utils.LocalColors

@Composable
fun SnoozeTile(
    state: SnoozeState,
    onClick: (interval: SnoozeInterval) -> Unit,
    onResumeClick: () -> Unit,
) {
    ConnectionTile(labelId = R.string.vpn_snooze) {
        if (state.active) {
            SnoozeTileActive(state.activeUntil) { onResumeClick() }
        } else {
            SnoozeTileDefault(onClick)
        }
    }
}

@Composable
private fun SnoozeTileDefault(onClick: (interval: SnoozeInterval) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        SnoozeTileButton(
            labelId = R.string.snooze_5_minutes,
            onClick = { onClick(SnoozeInterval.SNOOZE_SHORT_MS) },
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.weight(0.1f))
        SnoozeTileButton(
            labelId = R.string.snooze_15_minutes,
            onClick = { onClick(SnoozeInterval.SNOOZE_MEDIUM_MS) },
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.weight(0.1f))
        SnoozeTileButton(
            labelId = R.string.snooze_1_hour,
            onClick = { onClick(SnoozeInterval.SNOOZE_LONG_MS) },
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(modifier = Modifier.height(Space.SMALL))
    Text(
        text = stringResource(id = R.string.snooze_description),
        color = LocalColors.current.onSurface,
        fontSize = FontSize.Tiny,
    )
}

@Composable
private fun SnoozeTileActive(activeUntil: String?, onResumeClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        SnoozeTileButton(labelId = R.string.resume, onClick = onResumeClick, modifier = Modifier)
        Spacer(modifier = Modifier.height(Space.SMALL))
        Text(
            text = stringResource(id = R.string.paused_until).format(activeUntil),
            color = LocalColors.current.onSurface,
            fontSize = FontSize.Tiny,
        )
    }
}

@Composable
private fun SnoozeTileButton(labelId: Int, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .border(Width.OUTLINE, LocalColors.current.outlineVariant, MaterialTheme.shapes.medium)
            .padding(Space.SMALL)
            .clickable {
                onClick.invoke()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = labelId),
            fontSize = FontSize.Tiny,
            color = LocalColors.current.onSurfaceVariant,
        )
    }
}