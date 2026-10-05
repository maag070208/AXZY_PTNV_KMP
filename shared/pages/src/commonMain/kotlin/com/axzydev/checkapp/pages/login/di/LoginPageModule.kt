package com.axzydev.checkapp.pages.login.di

import com.axzydev.checkapp.pages.login.viewmodel.LoginViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val loginPageModule: Module = module {
    viewModel { LoginViewModel(get()) }
}
