package com.kape.notifications.di

import com.kape.notifications.data.NotificationChannelManager
import org.koin.dsl.module

val notificationModule = module {
    single { NotificationChannelManager(get()) }
}