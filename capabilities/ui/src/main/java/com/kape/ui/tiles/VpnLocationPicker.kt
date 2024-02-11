package com.kape.ui.tiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.ui.R
import com.kape.ui.text.SelectedRegionServerText
import com.kape.ui.text.SelectedRegionTitleText
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import com.kape.utils.vpnserver.VpnServer

@Composable
fun VpnLocationPicker(
    server: VpnServer,
    isConnected: Boolean,
    isOptimal: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(LocalColors.current.surfaceVariant, RoundedCornerShape(12.dp))
            .height(56.dp)
            .clickable {
                onClick()
            },
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
            SelectedRegionTitleText(content = stringResource(id = heading))

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
            tint = LocalColors.current.onSurface,
            modifier = Modifier
                .align(CenterVertically)
                .padding(end = 16.dp),
        )
    }
}