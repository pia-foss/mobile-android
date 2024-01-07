package com.kape.snooze.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kape.snooze.OnSnoozeReceiver
import com.kape.snooze.SnoozeHandler
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun snoozeModule(appModule: Module) = module {
    includes(appModule, localSnoozeModule)
}

val localSnoozeModule = module {
    single(named("set-snooze-pending-intent")) { provideSetSnoozePendingIntent(get()) }
    single(named("cancel-snooze-pending-intent")) { provideCancelSnoozePendingIntent(get()) }
    single {
        SnoozeHandler(
            get(),
            get(named("set-snooze-pending-intent")),
            get(named("cancel-snooze-pending-intent")),
            get(),
            get(),
        )
    }
}

private fun provideSetSnoozePendingIntent(context: Context): PendingIntent {
    return PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, OnSnoozeReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT,
    )
}

private fun provideCancelSnoozePendingIntent(context: Context): PendingIntent? {
    return PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, OnSnoozeReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
    )
}