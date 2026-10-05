package com.axzydev.checkapp.pages.guarddiscipline.di

import com.axzydev.checkapp.pages.guarddiscipline.viewmodel.GuardDisciplineViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val guardDisciplinePageModule: Module = module {
    viewModel { GuardDisciplineViewModel(get(), get()) }
}
