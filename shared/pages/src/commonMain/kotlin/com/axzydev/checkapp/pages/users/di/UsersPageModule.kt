package com.axzydev.checkapp.pages.users.di

import com.axzydev.checkapp.pages.users.viewmodel.UsersViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val usersPageModule: Module = module {
    viewModel { UsersViewModel(get(), get(), get(), get(), get(), get()) }
}
