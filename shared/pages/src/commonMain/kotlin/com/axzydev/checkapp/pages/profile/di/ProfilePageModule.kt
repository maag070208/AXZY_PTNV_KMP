package com.axzydev.checkapp.pages.profile.di

import com.axzydev.checkapp.pages.profile.viewmodel.ProfileViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profilePageModule: Module = module {
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
}
