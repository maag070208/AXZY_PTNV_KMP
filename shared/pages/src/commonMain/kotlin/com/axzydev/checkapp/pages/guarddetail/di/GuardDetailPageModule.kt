package com.axzydev.checkapp.pages.guarddetail.di

import com.axzydev.checkapp.pages.guarddetail.viewmodel.GuardDetailViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val guardDetailPageModule: Module = module {
    viewModel { GuardDetailViewModel(get()) }
}
