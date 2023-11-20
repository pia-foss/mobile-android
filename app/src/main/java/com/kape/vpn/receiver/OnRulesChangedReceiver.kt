package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kape.vpn.utils.NetworkListener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OnRulesChangedReceiver : BroadcastReceiver(), KoinComponent {

    private val networkListener: NetworkListener by inject()

    override fun onReceive(context: Context, intent: Intent) {
        networkListener.triggerUpdate()
    }
}