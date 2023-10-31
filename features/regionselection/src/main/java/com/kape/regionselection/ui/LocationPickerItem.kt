package com.kape.regionselection.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import com.kape.ui.elements.FavoriteIcon
import com.kape.ui.elements.MenuSeparator
import com.kape.ui.text.RegionSelectionText
import com.kape.ui.utils.getFlagResource
import com.kape.utils.server.Server

@Composable
fun LocationPickerItem(
    server: Server,
    isFavorite: Boolean,
    enableFavorite: Boolean,
    onClick: ((server: Server) -> Unit),
    onFavoriteClick: ((serverName: String) -> Unit),
) {
    Column {
        Row(
            modifier = Modifier
                .clickable {
                    onClick(server)
                }
                .height(56.dp)
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
            RegionSelectionText(content = server.name, modifier = Modifier.align(CenterVertically))
            Spacer(modifier = Modifier.weight(1f))
            if (enableFavorite) {
                FavoriteIcon(
                    isChecked = isFavorite,
                    modifier = Modifier
                        .align(CenterVertically)
                        .clickable {
                            onFavoriteClick(server.name)
                        },
                )
            }
        }
        MenuSeparator()
    }
}