package com.axzydev.checkapp.pages.sync.di

import com.axzydev.checkapp.pages.sync.viewmodel.SyncViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val syncPageModule: Module = module {
    viewModel { SyncViewModel(get()) }
}
