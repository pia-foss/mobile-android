package com.kape.connection.ui.tiles

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kape.connection.R
import com.kape.connection.ui.vm.ConnectionViewModel
import com.kape.connection.utils.SnoozeState
import com.kape.uicomponents.components.ConnectionTile
import com.kape.uicomponents.theme.*

// TODO: update state as needed; currently used for display purposes only

@Composable
fun SnoozeTile(state: SnoozeState, viewModel: ConnectionViewModel) {
    ConnectionTile(labelId = R.string.vpn_snooze) {
        if (state.active) {
            SnoozeTileActive(state.activeUntil) { viewModel.snooze(viewModel.SNOOZE_DEFAULT_MS) }
        } else {
            SnoozeTileDefault(viewModel)
        }
    }
}

@Composable
private fun SnoozeTileDefault(viewModel: ConnectionViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        SnoozeTileButton(
            labelId = R.string.snooze_5_minutes,
            onClick = { viewModel.snooze(viewModel.SNOOZE_SHORT_MS) },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.weight(0.1f))
        SnoozeTileButton(
            labelId = R.string.snooze_15_minutes,
            onClick = { viewModel.snooze(viewModel.SNOOZE_MEDIUM_MS) },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.weight(0.1f))
        SnoozeTileButton(
            labelId = R.string.snooze_1_hour,
            onClick = { viewModel.snooze(viewModel.SNOOZE_LONG_MS) },
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(Space.SMALL))
    Text(
        text = stringResource(id = R.string.snooze_description), color = Grey20,
        fontSize = FontSize.Tiny
    )
}

@Composable
private fun SnoozeTileActive(activeUntil: String?, onResumeClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        SnoozeTileButton(labelId = R.string.resume, onClick = onResumeClick, modifier = Modifier)
        Spacer(modifier = Modifier.height(Space.SMALL))
        Text(
            text = stringResource(id = R.string.paused_until).format(activeUntil), color = Grey20,
            fontSize = FontSize.Tiny
        )
    }
}

@Composable
private fun SnoozeTileButton(labelId: Int, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .border(Width.OUTLINE, Grey55, Shapes.medium)
            .padding(Space.SMALL)
            .clickable {
                onClick.invoke()
            }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = labelId),
            fontSize = FontSize.Tiny,
            color = Grey40,
        )
    }
}