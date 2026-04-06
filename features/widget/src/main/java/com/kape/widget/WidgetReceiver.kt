package com.kape.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.kape.contracts.ConnectionInfoProvider
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnlauncher.VpnLauncher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {

    private val vpnLauncher: VpnLauncher by inject()
    private val connectionInfoProvider: ConnectionInfoProvider by inject()
    private val usageProvider: UsageProvider by inject()

    override val glanceAppWidget: GlanceAppWidget
        get() = Widget(vpnLauncher, usageProvider, connectionInfoProvider)
}