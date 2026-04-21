package com.kape.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kape.data.DI
import com.kape.utils.NetworkConnectionListener
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Singleton(binds = [BroadcastReceiver::class])
@Named(DI.RULES_UPDATED_BROADCAST)
class OnRulesChangedReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val networkListener: NetworkConnectionListener by inject()

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        networkListener.triggerUpdate()
    }
}