package com.axzydev.checkapp.pages.zones.di

import com.axzydev.checkapp.pages.zones.viewmodel.ZonesViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val zonesPageModule: Module = module {
    viewModel { ZonesViewModel(get(), get(), get(), get()) }
}
