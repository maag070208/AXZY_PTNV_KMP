package com.axzydev.checkapp.pages.locations.di

import com.axzydev.checkapp.pages.locations.viewmodel.LocationsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val locationsPageModule: Module = module {
    viewModel { LocationsViewModel(get(), get(), get(), get(), get()) }
}
