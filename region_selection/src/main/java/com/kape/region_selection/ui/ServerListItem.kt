package com.kape.region_selection.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.kape.region_selection.R
import com.kape.region_selection.server.Server
import com.kape.uicomponents.theme.FontSize
import com.kape.uicomponents.theme.Height
import com.kape.uicomponents.theme.Space
import com.kape.uicomponents.theme.Width
import java.util.*

@Composable
fun ServerListItem(server: Server, onClick: ((server: Server) -> Unit)) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(Height.DEFAULT)
        .clickable {
            onClick(server)
        }
        .padding(horizontal = Space.NORMAL), verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(id = getFlagResource(LocalContext.current, server.iso)),
            contentDescription = "flag",
            modifier = Modifier
                .width(Width.FLAG)
                .height(Height.FLAG))
        Text(text = server.name, fontSize = FontSize.Normal, modifier = Modifier.padding(horizontal = Space.SMALL))

        if (server.isGeo) {
            Image(painter = painterResource(id = R.drawable.ic_geo_default),
                contentDescription = "geo",
                modifier = Modifier
                    .padding(end = Space.SMALL)
                    .width(Width.SERVER_ICON)
                    .height(Height.SERVER_ICON))
        }

        if (server.isAllowsPF) {
            Image(painter = painterResource(id = R.drawable.ic_port_forwarding),
                contentDescription = "port forwarding",
                modifier = Modifier
                    .padding(end = Space.SMALL)
                    .width(Width.SERVER_ICON)
                    .height(Height.SERVER_ICON))
        }

        // TODO: if server is selected - add appropriate image && update respective colors

        if (server.isOffline) {
            Image(painter = painterResource(id = R.drawable.ic_server_selected),
                contentDescription = "selected",
                modifier = Modifier
                    .padding(end = Space.SMALL)
                    .width(Width.SERVER_ICON)
                    .height(Height.SERVER_ICON))
        }


        Spacer(modifier = Modifier.weight(1f))
        Text(text = server.latency ?: "234 ms", fontSize = FontSize.Small, modifier = Modifier.padding(horizontal = Space.SMALL))
        Image(painter = painterResource(id = R.drawable.ic_favourite_default),
            contentDescription = "favourite",
            modifier = Modifier
                .width(Width.FAVOURITE)
                .height(Height.FAVOURITE))

    }
}

private fun getFlagResource(context: Context, serverIso: String): Int {
    val resName = String.format(Locale.US, "flag_%s", serverIso.replace(" ", "_").replace(",", "").lowercase())
    var flagResource: Int = context.resources.getIdentifier(resName, "drawable", context.packageName)
    if (flagResource == 0) {
        flagResource = R.drawable.flag_world
    }
    return flagResource
}