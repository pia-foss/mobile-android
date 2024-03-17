package com.kape.ui.tv.tiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import com.kape.ui.R
import com.kape.ui.tv.elements.TileButton
import com.kape.ui.tv.text.SelectedRegionServerText
import com.kape.ui.tv.text.SelectedRegionTitleText
import com.kape.ui.utils.getFlagResource
import com.kape.utils.vpnserver.VpnServer

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VpnLocationPicker(
    modifier: Modifier = Modifier,
    server: VpnServer,
    isConnected: Boolean,
    isOptimal: Boolean,
    onClick: () -> Unit,
) {
    TileButton(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(
                paddingValues = PaddingValues(horizontal = 16.dp),
            ),
        onClick = onClick,
    ) {
        Image(
            painter = painterResource(
                id = if (isOptimal) {
                    getFlagResource(
                        LocalContext.current,
                        "",
                    )
                } else {
                    getFlagResource(LocalContext.current, server.iso)
                },

            ),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 16.dp)
                .clip(CircleShape)
                .size(24.dp)
                .align(CenterVertically),
        )

        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.align(CenterVertically)) {
            val heading = if (isOptimal) {
                R.string.optimal_vpn_region
            } else {
                if (isConnected) R.string.current_vpn_region else R.string.selected_vpn_region
            }
            SelectedRegionTitleText(content = stringResource(id = heading).uppercase())

            Spacer(modifier = Modifier.height(4.dp))
            val name = if (isOptimal && !isConnected) {
                stringResource(id = R.string.automatic)
            } else {
                server.name
            }
            SelectedRegionServerText(content = name)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.ic_more_horizontal),
            contentDescription = null,
            modifier = Modifier
                .align(CenterVertically)
                .padding(end = 16.dp),
        )
    }
}