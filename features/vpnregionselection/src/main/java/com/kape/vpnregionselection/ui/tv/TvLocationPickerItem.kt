package com.kape.vpnregionselection.ui.tv

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kape.ui.mobile.elements.FavoriteIcon
import com.kape.ui.theme.getLatencyColor
import com.kape.ui.tv.elements.TileButton
import com.kape.ui.tv.text.RegionSelectionLatencyText
import com.kape.ui.tv.text.RegionSelectionNameText
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource

@Composable
fun TvLocationPickerItem(
    vpnServerIso: String,
    vpnServerName: String,
    vpnServerLatency: String?,
    vpnServerLatencyTimeout: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = { },
) {
    TileButton(
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                Image(
                    painter = painterResource(
                        id = getFlagResource(
                            context = LocalContext.current,
                            serverIso = vpnServerIso,
                        ),
                    ),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .requiredSize(40.dp)
                        .clip(CircleShape),
                )
                Spacer(modifier = Modifier.height(8.dp))
                RegionSelectionNameText(
                    content = vpnServerName,
                )
                Spacer(modifier = Modifier.height(8.dp))
                val latency = vpnServerLatency ?: vpnServerLatencyTimeout
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (latency.toInt() < vpnServerLatencyTimeout.toInt()) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    LocalColors.current.getLatencyColor(
                                        latency,
                                    ),
                                    shape = CircleShape,
                                ),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        RegionSelectionLatencyText(content = "${latency}ms")
                    } else {
                        RegionSelectionLatencyText(content = "")
                    }
                }
            }
            FavoriteIcon(
                isChecked = isFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp),
            )
        }
    }
}