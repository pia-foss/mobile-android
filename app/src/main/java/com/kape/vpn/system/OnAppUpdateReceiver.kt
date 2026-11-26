package com.kape.vpn.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebStorage
import com.kape.contracts.di.Qualifiers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

class OnAppUpdateReceiver :
    BroadcastReceiver(),
    KoinComponent {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().removeAllCookies(null)

        val pendingResult = goAsync()
        get<CoroutineScope>(named(Qualifiers.APP_SCOPE)).launch {
            try {
                get<OnAppUpdatedUseCase>()()
            } finally {
                pendingResult.finish()
            }
        }
    }
}