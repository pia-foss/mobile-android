package com.kape.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import com.kape.vpnconnect.utils.ConnectionStatus

class Widget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // TODO: to be implemented by https://polymoon.atlassian.net/browse/PIA-930
        provideContent {
            Size1WidgetContent()
        }
    }

    @Composable
    fun Size1WidgetContent() {
        GlanceTheme(colors = WidgetColors.colors) {
            Column(
                modifier = GlanceModifier.background(GlanceTheme.colors.background)
                    .padding(8.dp),
            ) {
                Image(
                    provider = ImageProvider(com.kape.ui.R.drawable.pia_medium),
                    contentDescription = null,
                    modifier = GlanceModifier.width(40.dp),
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                // TODO: proper implementation to follow: https://polymoon.atlassian.net/browse/PIA-1014
                WidgetConnectButton(ConnectionStatus.CONNECTED)
            }
        }
    }

    @Composable
    fun Size2WidgetContent() {
        GlanceTheme(colors = WidgetColors.colors) {
            Column(
                modifier = GlanceModifier.background(GlanceTheme.colors.background).width(160.dp)
                    .height(106.dp)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    provider = ImageProvider(com.kape.ui.R.drawable.pia_medium),
                    contentDescription = null,
                    modifier = GlanceModifier.width(40.dp),
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                // TODO: proper implementation to follow: https://polymoon.atlassian.net/browse/PIA-1014
                Row(
                    modifier = GlanceModifier.width(128.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Text(text = "Automatic Region", modifier = GlanceModifier.width(80.dp))
                    WidgetConnectButton(ConnectionStatus.CONNECTED)
                }
            }
        }
    }

    @Composable
    fun WidgetConnectButton(status: ConnectionStatus) {
        GlanceTheme(colors = WidgetColors.colors) {
            Box(
                modifier = GlanceModifier.width(48.dp).height(48.dp)
                    .background(getBackgroundForStatus(status)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = GlanceModifier.width(44.dp).height(44.dp)
                        .background(getOutlineForStatus(status)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = getIconForStatus(status),
                        contentDescription = null,
                        modifier = GlanceModifier.width(24.dp).height(24.dp),
                    )
                }
            }
        }
    }

    private fun getBackgroundForStatus(status: ConnectionStatus): ImageProvider {
        return when (status) {
            ConnectionStatus.CONNECTED -> ImageProvider(R.drawable.background_green)
            ConnectionStatus.CONNECTING,
            ConnectionStatus.DISCONNECTED,
            ConnectionStatus.RECONNECTING,
            -> ImageProvider(R.drawable.background_yellow)
        }
    }

    private fun getOutlineForStatus(status: ConnectionStatus): ImageProvider {
        return when (status) {
            ConnectionStatus.CONNECTED -> ImageProvider(R.drawable.outline_green)
            ConnectionStatus.CONNECTING,
            ConnectionStatus.DISCONNECTED,
            ConnectionStatus.RECONNECTING,
            -> ImageProvider(R.drawable.outline_yellow)
        }
    }

    private fun getIconForStatus(status: ConnectionStatus): ImageProvider {
        return when (status) {
            ConnectionStatus.CONNECTED -> ImageProvider(R.drawable.ic_power_green)
            ConnectionStatus.CONNECTING,
            ConnectionStatus.DISCONNECTED,
            ConnectionStatus.RECONNECTING,
            -> ImageProvider(R.drawable.ic_power_yellow)
        }
    }
}