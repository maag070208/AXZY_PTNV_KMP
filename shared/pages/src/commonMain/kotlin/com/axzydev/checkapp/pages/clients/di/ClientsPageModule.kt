package com.axzydev.checkapp.pages.clients.di

import com.axzydev.checkapp.pages.clients.viewmodel.ClientsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val clientsPageModule: Module = module {
    viewModel { ClientsViewModel(get(), get(), get(), get()) }
}
