package com.kape.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {

    private val vpnLauncher: VpnLauncher by inject()
    private val connectionManager: ConnectionManager by inject()

    override val glanceAppWidget: GlanceAppWidget
        get() = Widget(vpnLauncher, connectionManager)
}