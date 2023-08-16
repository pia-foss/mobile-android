package com.kape.notifications.di

import com.kape.notifications.data.NotificationChannelManager
import com.kape.notifications.data.NotificationPermissionManager
import org.koin.dsl.module

val notificationModule = module {
    single { NotificationChannelManager(get()) }
    single { NotificationPermissionManager(get()) }
}