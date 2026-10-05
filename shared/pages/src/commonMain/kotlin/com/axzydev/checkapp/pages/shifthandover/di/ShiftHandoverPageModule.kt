package com.axzydev.checkapp.pages.shifthandover.di

import com.axzydev.checkapp.pages.shifthandover.viewmodel.ShiftHandoverViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val shiftHandoverPageModule: Module = module {
    viewModel { ShiftHandoverViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
