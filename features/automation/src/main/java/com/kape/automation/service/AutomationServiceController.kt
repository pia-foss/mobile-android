package com.kape.automation.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.kape.automation.domain.AutomationServiceControl
import com.kape.contracts.AutomationServiceLauncher
import org.koin.core.annotation.Singleton

@Singleton(binds = [AutomationServiceLauncher::class, AutomationServiceControl::class])
class AutomationServiceController(
    private val context: Context,
) : AutomationServiceLauncher,
    AutomationServiceControl {
    private val intent get() = Intent(context, AutomationService::class.java)

    override fun start() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: IllegalStateException) {
            // Android 12+ doesn't allow starting a foreground service from the background. It is
            // started again the next time the app opens.
        }
    }

    override fun stop() {
        context.stopService(intent)
    }
}