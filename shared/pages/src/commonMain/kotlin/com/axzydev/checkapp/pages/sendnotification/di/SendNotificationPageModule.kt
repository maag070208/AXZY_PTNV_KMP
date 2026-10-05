package com.axzydev.checkapp.pages.sendnotification.di

import com.axzydev.checkapp.pages.sendnotification.viewmodel.SendNotificationViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sendNotificationPageModule: Module = module {
    viewModel { SendNotificationViewModel(get()) }
}
