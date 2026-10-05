package com.axzydev.checkapp.features.notifications.di

import com.axzydev.checkapp.features.notifications.model.CreateScheduledNotificationUseCase
import com.axzydev.checkapp.features.notifications.model.DeleteScheduledNotificationUseCase
import com.axzydev.checkapp.features.notifications.model.ListNotificationsUseCase
import com.axzydev.checkapp.features.notifications.model.ListScheduledNotificationsUseCase
import com.axzydev.checkapp.features.notifications.model.MarkAllNotificationsReadUseCase
import com.axzydev.checkapp.features.notifications.model.MarkNotificationReadUseCase
import com.axzydev.checkapp.features.notifications.model.SendNotificationUseCase
import com.axzydev.checkapp.features.notifications.model.ToggleScheduledNotificationUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val notificationsModule: Module = module {
    factory { ListNotificationsUseCase(get()) }
    factory { MarkNotificationReadUseCase(get()) }
    factory { MarkAllNotificationsReadUseCase(get()) }
    factory { SendNotificationUseCase(get()) }
    factory { ListScheduledNotificationsUseCase(get()) }
    factory { CreateScheduledNotificationUseCase(get()) }
    factory { ToggleScheduledNotificationUseCase(get()) }
    factory { DeleteScheduledNotificationUseCase(get()) }
}
