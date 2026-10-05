package com.axzydev.checkapp.entities.schedulednotification.di

import com.axzydev.checkapp.entities.schedulednotification.data.DefaultScheduledNotificationRepository
import com.axzydev.checkapp.entities.schedulednotification.data.ScheduledNotificationRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val scheduledNotificationModule: Module = module {
    single<ScheduledNotificationRepository> { DefaultScheduledNotificationRepository(get()) }
}
