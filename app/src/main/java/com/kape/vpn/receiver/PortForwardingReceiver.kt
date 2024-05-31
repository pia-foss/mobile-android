package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.vpnconnect.domain.ConnectionDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class PortForwardingReceiver : BroadcastReceiver(), CoroutineScope, KoinComponent {

    private val portForwardingUseCase: PortForwardingUseCase by inject()
    private val connectionDataSource: ConnectionDataSource by inject()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) {
            return
        }

        launch {
            try {
                portForwardingUseCase.bindPort(connectionDataSource.getVpnToken())
            } catch (exception: IOException) {
                // no-op
            } catch (exception: IllegalStateException) {
                // no-op
            }
        }
    }
}