package com.kape.vpnregionselection.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kape.data.AUTO_KEY
import com.kape.data.vpnserver.VpnServer
import com.kape.ui.R

@Composable
internal fun VpnServer.displayName(): String = if (key == AUTO_KEY) stringResource(R.string.optimal_vpn_region) else name

@Composable
internal fun VpnServer.displayIso(): String = if (key == AUTO_KEY) stringResource(R.string.automatic_iso) else iso