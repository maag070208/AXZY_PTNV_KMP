package com.axzydev.checkapp.pages.recurring.di

import com.axzydev.checkapp.pages.recurring.viewmodel.RecurringViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val recurringPageModule: Module = module {
    viewModel { RecurringViewModel(get(), get()) }
}
