package com.kape.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
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
import com.kape.uicomponents.components.AppBar
import com.kape.uicomponents.components.AppBarColors
import com.kape.uicomponents.components.AppBarState
import com.kape.uicomponents.theme.FontSize
import com.kape.uicomponents.theme.Grey20
import com.kape.uicomponents.theme.Grey55
import com.kape.uicomponents.theme.Space
import org.koin.androidx.compose.viewModel

@Composable
fun ProfileScreen() {

    val viewModel: ProfileViewModel by viewModel()
    val state by remember(viewModel) { viewModel.screenState }.collectAsState()

    Column {
        AppBar(
            onClick = {
                viewModel.navigateBack()
            },
            state = AppBarState(
                stringResource(id = R.string.account),
                AppBarColors.Default,
                showLogo = false,
                showMenu = false,
                showOverflow = false
            )
        )

        if (state.loading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = Space.NORMAL)
            ) {
                Spacer(modifier = Modifier.height(Space.MEDIUM))
                Text(color = Grey55, text = stringResource(id = R.string.username), fontSize = FontSize.Small)
                Text(
                    color = Grey20,
                    text = state.username
                )
                Spacer(modifier = Modifier.height(Space.MEDIUM))
                Text(color = Grey55, text = stringResource(id = R.string.message_other_devices), fontSize = FontSize.Small)
                Spacer(modifier = Modifier.height(Space.NORMAL))
                Row {
                    Text(
                        color = Grey55,
                        text = stringResource(id = if (state.expired) R.string.message_expired else R.string.message_expiration),
                        fontSize = FontSize.Small
                    )
                    Spacer(modifier = Modifier.width(Space.MINI))
                    Text(
                        color = Grey20,
                        text = if (state.expired) stringResource(id = R.string.subscription_status_expired) else state.expirationDate,
                        fontSize = FontSize.Small
                    )
                }
                Spacer(modifier = Modifier.height(Space.NORMAL))
            }
        }


    }
}
