package com.axzydev.checkapp.pages.bulkprint.di

import com.axzydev.checkapp.pages.bulkprint.viewmodel.BulkPrintViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val bulkPrintPageModule: Module = module {
    viewModel { BulkPrintViewModel(get(), get()) }
}
