package com.axzydev.checkapp.pages.home.di

import com.axzydev.checkapp.pages.home.viewmodel.HomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homePageModule: Module = module {
    viewModel { HomeViewModel(get()) }
}
