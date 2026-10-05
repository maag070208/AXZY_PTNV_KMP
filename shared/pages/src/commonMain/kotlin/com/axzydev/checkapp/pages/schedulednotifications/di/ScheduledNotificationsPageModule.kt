package com.axzydev.checkapp.pages.schedulednotifications.di

import com.axzydev.checkapp.pages.schedulednotifications.viewmodel.ScheduledNotificationsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val scheduledNotificationsPageModule: Module = module {
    viewModel { ScheduledNotificationsViewModel(get(), get(), get(), get()) }
}
