package com.kape.connection.ui.tiles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kape.connection.R
import com.kape.connection.utils.MapPositionCalculator
import com.kape.core.server.Server
import com.kape.uicomponents.theme.*

@Composable
fun RegionInformationTile(server: Server, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Space.NORMAL)
            .clickable {
                onClick.invoke()
            }
    ) {
        Image(
            modifier = Modifier
                .padding(Space.NORMAL)
                .width(Width.REGION_INFO_MAP)
                .height(Height.REGION_INFO_MAP)
                .align(Alignment.CenterEnd),
            painter = painterResource(id = R.drawable.map_full),
            contentDescription = stringResource(
                id = R.string.map
            ),
            contentScale = ContentScale.FillBounds
        )

        MapLocation(
            server = server, modifier = Modifier
                .padding(Space.NORMAL)
                .width(Width.REGION_INFO_MAP)
                .height(Height.REGION_INFO_MAP)
                .align(Alignment.CenterEnd)
        )

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(Space.NORMAL)

        ) {
            Text(
                text = stringResource(id = R.string.current_region),
                color = Grey55,
                fontSize = FontSize.Small
            )
            Spacer(modifier = Modifier.height(Space.MINI))
            Text(text = server.name, color = Grey20, fontSize = FontSize.Normal)
        }
    }
}

@Composable
fun MapLocation(server: Server, modifier: Modifier) {
    val widthInPx = with(LocalDensity.current) { Width.REGION_INFO_MAP.toPx() }
    val heightInPx = with(LocalDensity.current) { Height.REGION_INFO_MAP.toPx() }
    val diameterInPx = with(LocalDensity.current) { Square.REGION_PIN.toPx() }
    val padding =
        MapPositionCalculator().getCurrentLocationPadding(
            server,
            widthInPx,
            heightInPx,
            diameterInPx
        )

    Column(
        modifier = modifier
            .padding(start = getDp(pixels = padding.start), top = getDp(pixels = padding.top))
    ) {
        Box(
            modifier = Modifier
                .size(Square.REGION_PIN)
                .background(color = ConnectedStart, shape = CircleShape)
                .border(width = 1.dp, color = ConnectedEnd, shape = CircleShape)
        )
    }
}

@Composable
private fun getDp(pixels: Float) = with(LocalDensity.current) {
    pixels.toDp()
}