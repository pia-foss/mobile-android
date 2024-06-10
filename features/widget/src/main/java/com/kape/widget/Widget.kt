package com.kape.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.glance.action.clickable
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
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import com.kape.vpnlauncher.VpnLauncher

class Widget(
    private val vpnLauncher: VpnLauncher,
    private val connectionManager: ConnectionManager,
    private val usageProvider: UsageProvider,
) : GlanceAppWidget() {

    companion object {
        private val size1 = DpSize(80.dp, 106.dp)
        private val size2 = DpSize(160.dp, 106.dp)
        private val size3 = DpSize(160.dp, 186.dp)
        private val size4 = DpSize(240.dp, 186.dp)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val connectionState = connectionManager.connectionStatus.collectAsState()
            val name = connectionManager.serverName.collectAsState()
            val downloadSpeed = usageProvider.widgetDownloadSpeed.value
            val uploadSpeed = usageProvider.widgetUploadSpeed.value
            when (LocalSize.current) {
                size1 -> Size1WidgetContent(connectionState.value)
                size2 -> Size2WidgetContent(connectionState.value, name.value)
                size3 -> Size3WidgetContent(
                    connectionState.value,
                    name.value,
                    downloadSpeed,
                    uploadSpeed,
                )

                size4 -> Size4WidgetContent(
                    connectionState.value,
                    name.value,
                    downloadSpeed,
                    uploadSpeed,
                )

                else ->
                    throw IllegalArgumentException("Invalid size not matching the provided ones: ${LocalSize.current}")
            }
        }
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(size1, size2, size3, size4),
    )

    @Composable
    fun Size1WidgetContent(status: ConnectionStatus) {
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
                WidgetConnectButton(status)
            }
        }
    }

    @Composable
    fun Size2WidgetContent(status: ConnectionStatus, name: String) {
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
                Row(
                    modifier = GlanceModifier.width(128.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Text(
                        text = name,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 14.sp,
                        ),
                        modifier = GlanceModifier.width(80.dp),
                    )
                    WidgetConnectButton(status)
                }
            }
        }
    }

    @Composable
    fun Size3WidgetContent(
        status: ConnectionStatus,
        name: String,
        downloadSpeed: String,
        uploadSpeed: String,
    ) {
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
                Row(
                    modifier = GlanceModifier.width(128.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Text(
                        text = name,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 14.sp,
                        ),
                        modifier = GlanceModifier.width(80.dp),
                    )
                    WidgetConnectButton(status)
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
                            text = downloadSpeed,
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
                            text = uploadSpeed,
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
    fun Size4WidgetContent(
        status: ConnectionStatus,
        name: String,
        downloadSpeed: String,
        uploadSpeed: String,
    ) {
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
                Row(
                    modifier = GlanceModifier.width(240.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Text(
                        text = name,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 14.sp,
                        ),
                        modifier = GlanceModifier.width(120.dp).padding(start = 16.dp),
                    )
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(end = 16.dp),
                        horizontalAlignment = Alignment.Horizontal.End,
                    ) {
                        WidgetConnectButton(status)
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
                            text = downloadSpeed,
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
                            text = uploadSpeed,
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
                    .background(getBackgroundForStatus(status)).clickable {
                        if (vpnLauncher.isVpnConnected()) {
                            vpnLauncher.stopVpn()
                        } else {
                            vpnLauncher.launchVpn()
                        }
                    },
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
            ConnectionStatus.DISCONNECTING,
            ConnectionStatus.RECONNECTING,
            -> ImageProvider(R.drawable.background_yellow)

            ConnectionStatus.ERROR -> ImageProvider(R.drawable.background_red)
        }
    }

    private fun getOutlineForStatus(status: ConnectionStatus): ImageProvider {
        return when (status) {
            ConnectionStatus.CONNECTED -> ImageProvider(R.drawable.outline_green)
            ConnectionStatus.CONNECTING,
            ConnectionStatus.DISCONNECTED,
            ConnectionStatus.DISCONNECTING,
            ConnectionStatus.RECONNECTING,
            -> ImageProvider(R.drawable.outline_yellow)

            ConnectionStatus.ERROR -> ImageProvider(R.drawable.outline_red)
        }
    }

    private fun getIconForStatus(status: ConnectionStatus): ImageProvider {
        return when (status) {
            ConnectionStatus.CONNECTED -> ImageProvider(R.drawable.ic_power_green)
            ConnectionStatus.CONNECTING,
            ConnectionStatus.DISCONNECTED,
            ConnectionStatus.DISCONNECTING,
            ConnectionStatus.RECONNECTING,
            -> ImageProvider(R.drawable.ic_power_yellow)

            ConnectionStatus.ERROR -> ImageProvider(R.drawable.ic_power_red)
        }
    }
}