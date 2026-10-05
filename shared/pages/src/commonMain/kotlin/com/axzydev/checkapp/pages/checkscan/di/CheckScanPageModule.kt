package com.axzydev.checkapp.pages.checkscan.di

import com.axzydev.checkapp.pages.checkscan.viewmodel.CheckScanViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val checkScanPageModule: Module = module {
    viewModel { CheckScanViewModel(get(), get()) }
}
