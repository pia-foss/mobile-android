package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kape.utils.NetworkConnectionListener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OnRulesChangedReceiver : BroadcastReceiver(), KoinComponent {

    private val networkListener: NetworkConnectionListener by inject()

    override fun onReceive(context: Context, intent: Intent) {
        networkListener.triggerUpdate()
    }
}