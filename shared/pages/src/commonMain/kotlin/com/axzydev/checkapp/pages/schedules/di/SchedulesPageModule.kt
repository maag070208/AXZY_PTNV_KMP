package com.axzydev.checkapp.pages.schedules.di

import com.axzydev.checkapp.pages.schedules.viewmodel.SchedulesViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val schedulesPageModule: Module = module {
    viewModel { SchedulesViewModel(get(), get(), get(), get()) }
}
