package com.axzydev.checkapp.pages.kardex.di

import com.axzydev.checkapp.pages.kardex.viewmodel.KardexViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val kardexPageModule: Module = module {
    viewModel { KardexViewModel(get(), get(), get()) }
}
