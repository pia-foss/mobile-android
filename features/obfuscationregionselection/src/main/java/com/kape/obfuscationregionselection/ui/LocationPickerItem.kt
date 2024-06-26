package com.kape.obfuscationregionselection.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.unit.dp
import com.kape.ui.mobile.elements.FavoriteIcon
import com.kape.ui.mobile.elements.Separator
import com.kape.ui.mobile.text.RegionSelectionText
import com.kape.ui.utils.getFlagResource
import com.kape.utils.shadowsocksserver.ShadowsocksServer

@Composable
fun LocationPickerItem(
    server: ShadowsocksServer,
    isFavorite: Boolean,
    enableFavorite: Boolean,
    onClick: ((server: ShadowsocksServer) -> Unit),
    onFavoriteShadowsocksClick: ((shadowsocksServerName: String) -> Unit),
) {
    Column {
        Row(
            modifier = Modifier
                .clickable {
                    onClick(server)
                }
                .defaultMinSize(minHeight = 56.dp)
                .padding(16.dp),
        ) {
            Image(
                painter = painterResource(
                    id = getFlagResource(LocalContext.current, server.iso),
                ),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.align(CenterVertically).weight(12f)) {
                RegionSelectionText(content = server.region)
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            if (enableFavorite) {
                FavoriteIcon(
                    isChecked = isFavorite,
                    modifier = Modifier
                        .align(CenterVertically)
                        .clickable {
                            onFavoriteShadowsocksClick(server.region)
                        },
                )
            }
        }
        Separator()
    }
}