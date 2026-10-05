package com.axzydev.checkapp.pages.notifications.di

import com.axzydev.checkapp.pages.notifications.viewmodel.NotificationsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notificationsPageModule: Module = module {
    viewModel { NotificationsViewModel(get(), get(), get()) }
}
