package com.kape.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.kape.vpnconnect.utils.ConnectionStatus

class Widget : GlanceAppWidget() {

    companion object {
        private val size1 = DpSize(80.dp, 106.dp)
        private val size2 = DpSize(160.dp, 106.dp)
        private val size3 = DpSize(160.dp, 186.dp)
        private val size4 = DpSize(240.dp, 186.dp)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            when (LocalSize.current) {
                size1 -> Size1WidgetContent()
                size2 -> Size2WidgetContent()
                size3 -> Size3WidgetContent()
                size4 -> Size4WidgetContent()
                else ->
                    throw IllegalArgumentException("Invalid size not matching the provided ones: ${LocalSize.current}")
            }
        }
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(size1, size2, size3, size4),
    )

    @Composable
    fun Size1WidgetContent() {
        GlanceTheme(colors = WidgetColors.colors) {
            Column(
                modifier = GlanceModifier.background(GlanceTheme.colors.background).padding(8.dp)
                    .width(80.dp)
                    .height(106.dp),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
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
                modifier = GlanceModifier.background(GlanceTheme.colors.background).padding(8.dp)
                    .width(160.dp)
                    .height(106.dp),
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
    fun Size3WidgetContent() {
        GlanceTheme(colors = WidgetColors.colors) {
            Column(
                modifier = GlanceModifier.background(GlanceTheme.colors.background).padding(8.dp)
                    .width(160.dp)
                    .height(186.dp),
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
                Spacer(modifier = GlanceModifier.height(16.dp))
                Box(
                    modifier = GlanceModifier.height(0.5.dp).fillMaxWidth()
                        .padding(horizontal = 24.dp).background(GlanceTheme.colors.outline),
                ) {}
                Spacer(modifier = GlanceModifier.height(16.dp))
                Row(
                    modifier = GlanceModifier.width(128.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Column(
                        modifier = GlanceModifier.width(64.dp),
                        horizontalAlignment = Alignment.Horizontal.Start,
                    ) {
                        Image(
                            provider = ImageProvider(com.kape.ui.R.drawable.ic_download),
                            contentDescription = null,
                        )
                        Text(
                            text = "354 MB/s",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 12.sp,
                            ),
                        )
                    }
                    Column(
                        modifier = GlanceModifier.width(64.dp),
                        horizontalAlignment = Alignment.Horizontal.End,
                    ) {
                        Image(
                            provider = ImageProvider(com.kape.ui.R.drawable.ic_upload),
                            contentDescription = null,
                        )
                        Text(
                            text = "12 KB/s",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 12.sp,
                            ),
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun Size4WidgetContent() {
        GlanceTheme(colors = WidgetColors.colors) {
            Column(
                modifier = GlanceModifier.background(GlanceTheme.colors.background).padding(8.dp)
                    .width(240.dp)
                    .height(186.dp),
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
                    modifier = GlanceModifier.width(240.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Text(
                        text = "Automatic Region",
                        modifier = GlanceModifier.width(120.dp).padding(start = 16.dp),
                    )
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(end = 16.dp),
                        horizontalAlignment = Alignment.Horizontal.End,
                    ) {
                        WidgetConnectButton(ConnectionStatus.CONNECTED)
                    }
                }
                Spacer(modifier = GlanceModifier.height(16.dp))
                Box(
                    modifier = GlanceModifier.height(0.5.dp).fillMaxWidth()
                        .padding(horizontal = 24.dp).background(GlanceTheme.colors.outline),
                ) {}
                Spacer(modifier = GlanceModifier.height(16.dp))
                Row(
                    modifier = GlanceModifier.width(200.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Column(
                        modifier = GlanceModifier.width(100.dp),
                        horizontalAlignment = Alignment.Horizontal.Start,
                    ) {
                        Text(
                            text = LocalContext.current.getString(com.kape.ui.R.string.download)
                                .uppercase(),
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 12.sp,
                            ),
                        )
                        Text(
                            text = "354 MB/s",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 12.sp,
                            ),
                        )
                    }
                    Column(
                        modifier = GlanceModifier.width(100.dp),
                        horizontalAlignment = Alignment.Horizontal.End,
                    ) {
                        Text(
                            text = LocalContext.current.getString(com.kape.ui.R.string.upload)
                                .uppercase(),
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 12.sp,
                            ),
                        )
                        Text(
                            text = "12 KB/s",
                            style = TextStyle(
                                color = GlanceTheme.colors.primary,
                                fontSize = 12.sp,
                            ),
                        )
                    }
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