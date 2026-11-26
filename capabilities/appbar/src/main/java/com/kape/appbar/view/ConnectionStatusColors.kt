package com.kape.appbar.view

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.kape.data.ConnectionStatus
import com.kape.ui.theme.statusBarConnected
import com.kape.ui.theme.statusBarConnecting
import com.kape.ui.theme.statusBarDefault
import com.kape.ui.theme.statusBarError

fun ConnectionStatus.topBarColor(scheme: ColorScheme): Color =
    when (this) {
        ConnectionStatus.CONNECTED -> scheme.statusBarConnected()
        ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> scheme.statusBarConnecting()
        ConnectionStatus.DISCONNECTED, ConnectionStatus.DISCONNECTING -> scheme.statusBarDefault(scheme)
        ConnectionStatus.ERROR, ConnectionStatus.PAUSED -> scheme.statusBarError()
    }