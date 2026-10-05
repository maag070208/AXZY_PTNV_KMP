package com.axzydev.checkapp.pages.maintenance.di

import com.axzydev.checkapp.pages.maintenance.viewmodel.MaintenanceViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val maintenancePageModule: Module = module {
    viewModel { MaintenanceViewModel(get(), get(), get()) }
}
