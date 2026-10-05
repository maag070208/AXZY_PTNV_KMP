package com.axzydev.checkapp.pages.checkreport.di

import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val checkReportPageModule: Module = module {
    viewModel { CheckReportViewModel(get(), get(), get(), get(), get()) }
}
