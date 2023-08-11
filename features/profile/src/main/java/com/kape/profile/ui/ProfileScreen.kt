package com.kape.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.kape.profile.R
import com.kape.profile.ui.vm.ProfileViewModel
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Space
import com.kape.ui.utils.LocalColors
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by remember(viewModel) { viewModel.screenState }.collectAsState()

    Column {
        // TODO: Iva! Add AppBar
        if (state.loading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = Space.NORMAL),
            ) {
                Spacer(modifier = Modifier.height(Space.MEDIUM))
                Text(
                    color = LocalColors.current.outlineVariant,
                    text = stringResource(id = R.string.username),
                    fontSize = FontSize.Small
                )
                Text(
                    color = LocalColors.current.onSurface,
                    text = state.username,
                )
                Spacer(modifier = Modifier.height(Space.MEDIUM))
                Text(
                    color = LocalColors.current.outlineVariant,
                    text = stringResource(id = R.string.message_other_devices),
                    fontSize = FontSize.Small
                )
                Spacer(modifier = Modifier.height(Space.NORMAL))
                Row {
                    Text(
                        color = LocalColors.current.outlineVariant,
                        text = stringResource(id = if (state.expired) R.string.message_expired else R.string.message_expiration),
                        fontSize = FontSize.Small,
                    )
                    Spacer(modifier = Modifier.width(Space.MINI))
                    Text(
                        color = LocalColors.current.onSurface,
                        text = if (state.expired) stringResource(id = R.string.subscription_status_expired) else state.expirationDate,
                        fontSize = FontSize.Small,
                    )
                }
                Spacer(modifier = Modifier.height(Space.NORMAL))
            }
        }
    }
}
