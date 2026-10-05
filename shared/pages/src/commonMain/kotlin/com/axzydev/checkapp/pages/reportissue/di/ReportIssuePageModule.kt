package com.axzydev.checkapp.pages.reportissue.di

import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val reportIssuePageModule: Module = module {
    viewModel { ReportIssueViewModel(get(), get(), get(), get()) }
}
