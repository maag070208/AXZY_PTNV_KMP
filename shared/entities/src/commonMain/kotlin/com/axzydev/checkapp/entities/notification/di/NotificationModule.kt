package com.axzydev.checkapp.entities.notification.di

import com.axzydev.checkapp.entities.notification.data.DefaultNotificationRepository
import com.axzydev.checkapp.entities.notification.data.NotificationRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val notificationModule: Module = module {
    single<NotificationRepository> { DefaultNotificationRepository(get()) }
}
