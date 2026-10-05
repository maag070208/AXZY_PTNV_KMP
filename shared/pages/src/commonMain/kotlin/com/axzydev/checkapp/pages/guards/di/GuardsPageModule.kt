package com.axzydev.checkapp.pages.guards.di

import com.axzydev.checkapp.pages.guards.viewmodel.GuardsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val guardsPageModule: Module = module {
    viewModel { GuardsViewModel(get(), get()) }
}
