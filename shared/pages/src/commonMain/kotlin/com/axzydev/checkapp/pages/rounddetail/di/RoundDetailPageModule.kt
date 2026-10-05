package com.axzydev.checkapp.pages.rounddetail.di

import com.axzydev.checkapp.pages.rounddetail.viewmodel.RoundDetailViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val roundDetailPageModule: Module = module {
    viewModel { RoundDetailViewModel(get(), get(), get(), get()) }
}
