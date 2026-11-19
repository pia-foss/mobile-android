package com.kape.vpn.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kape.contracts.di.Qualifiers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

class OnBootReceiver :
    BroadcastReceiver(),
    KoinComponent {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action !in BOOT_ACTIONS) return
        val pendingResult = goAsync()
        get<CoroutineScope>(named(Qualifiers.APP_SCOPE)).launch {
            try {
                get<OnBootUseCase>()()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

// QUICKBOOT_POWERON is what some HTC devices send instead of BOOT_COMPLETED.
private val BOOT_ACTIONS = setOf(Intent.ACTION_BOOT_COMPLETED, "android.intent.action.QUICKBOOT_POWERON")