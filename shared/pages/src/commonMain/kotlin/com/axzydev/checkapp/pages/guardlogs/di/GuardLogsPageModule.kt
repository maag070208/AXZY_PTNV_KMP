package com.axzydev.checkapp.pages.guardlogs.di

import com.axzydev.checkapp.pages.guardlogs.viewmodel.GuardLogsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val guardLogsPageModule: Module = module {
    viewModel { GuardLogsViewModel(get()) }
}
