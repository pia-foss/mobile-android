package com.kape.dedicatedip.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kape.appbar.view.NavigationAppBar
import com.kape.appbar.viewmodel.AppBarViewModel
import com.kape.dedicatedip.R
import com.kape.regionselection.ui.getLatencyTextColor
import com.kape.ui.theme.FontSize
import com.kape.ui.theme.Height
import com.kape.ui.theme.Space
import com.kape.ui.theme.Width
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import com.kape.utils.server.Server
import com.privateinternetaccess.regions.REGIONS_PING_TIMEOUT
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DedicatedIpScreen() {
    val appBarViewModel: AppBarViewModel = koinViewModel<AppBarViewModel>().apply {
        appBarText(stringResource(id = R.string.dedicated_ip_title))
    }
    Scaffold(
        topBar = {
            NavigationAppBar(
                viewModel = appBarViewModel,
                onLeftButtonClick = {
                    // TODO: implement
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LocalColors.current.onPrimary)
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.dedicated_ip_title),
                    fontSize = FontSize.Title,
                    color = LocalColors.current.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.dedicated_ip_description),
                    fontSize = FontSize.Small,
                    color = LocalColors.current.onSurface,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    border = BorderStroke(1.dp, color = LocalColors.current.primary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    var text = remember {
                        mutableStateOf(TextFieldValue(""))
                    }
                    Row {
                        TextField(
                            value = text.value,
                            onValueChange = {
                                text.value = it
                            },
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.dedicated_ip_hint),
                                    color = LocalColors.current.onSurface,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(6.5f),
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(textColor = LocalColors.current.onSurface),
                        )
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .align(CenterVertically)
                                .weight(3.5f),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                text = stringResource(id = R.string.activate),
                                color = LocalColors.current.onSurface,
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun DipItem(server: Server) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
        ) {
            Box {
                Icon(
                    painter = painterResource(
                        id = getFlagResource(
                            LocalContext.current,
                            server.iso,
                        ),
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Center)
                        .width(Width.FLAG)
                        .height(Height.FLAG),
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_dip_badge),
                    contentDescription = null,
                    modifier = Modifier
                        .align(TopEnd)
                        .size(14.dp),
                )
            }
            Text(
                text = server.name,
                fontSize = FontSize.Normal,
                modifier = Modifier.padding(horizontal = Space.SMALL),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (server.latency != null && server.latency!!.toInt() < REGIONS_PING_TIMEOUT) {
                    stringResource(id = com.kape.regionselection.R.string.latency_to_format).format(
                        server.latency,
                    )
                } else {
                    ""
                },
                fontSize = FontSize.Small,
                color = getLatencyTextColor(server.latency),
                modifier = Modifier.padding(horizontal = Space.SMALL),
            )
            IconButton(
                onClick = {
                    // TODO: Implement when ViewModel is ready
                },
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(id = com.kape.regionselection.R.string.favorite),
                    modifier = Modifier
                        .width(Width.FAVOURITE)
                        .height(Height.FAVOURITE),
                )
            }
        }
        Divider(
            color = LocalColors.current.outline,
        )
    }
}