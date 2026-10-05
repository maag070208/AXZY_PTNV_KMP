package com.axzydev.checkapp.pages.incidents.di

import com.axzydev.checkapp.pages.incidents.viewmodel.IncidentsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val incidentsPageModule: Module = module {
    viewModel { IncidentsViewModel(get(), get(), get()) }
}
