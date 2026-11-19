package com.kape.connection.ui.tiles

import com.kape.data.vpnserver.VpnServer

interface TileActions {
    fun onRegionClick()

    fun onShadowsocksRegionClick()

    fun onQuickConnect(server: VpnServer)

    fun onSnooze(intervalMillis: Int)

    fun onResumeSnooze()

    fun onKillSwitchClick()

    fun onAutomationClick()

    fun onProtocolsClick()
}

object NoTileActions : TileActions {
    override fun onRegionClick() = Unit

    override fun onShadowsocksRegionClick() = Unit

    override fun onQuickConnect(server: VpnServer) = Unit

    override fun onSnooze(intervalMillis: Int) = Unit

    override fun onResumeSnooze() = Unit

    override fun onKillSwitchClick() = Unit

    override fun onAutomationClick() = Unit

    override fun onProtocolsClick() = Unit
}