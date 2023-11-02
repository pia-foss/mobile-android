package com.kape.connection.ui.tiles

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.connection.R
import com.kape.ui.text.SelectedRegionServerText
import com.kape.ui.text.SelectedRegionTitleText
import com.kape.ui.utils.LocalColors
import com.kape.ui.utils.getFlagResource
import com.kape.utils.server.Server

@Composable
fun LocationPicker(server: Server, isConnected: Boolean, onClick: () -> Unit) {
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
                id = getFlagResource(LocalContext.current, server.iso),
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
            SelectedRegionTitleText(content = stringResource(id = if (isConnected) R.string.current_region else R.string.selected_region))
            Spacer(modifier = Modifier.height(4.dp))
            SelectedRegionServerText(content = server.name)
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
