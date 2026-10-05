package com.axzydev.checkapp.pages.uniformcheck.di

import com.axzydev.checkapp.pages.uniformcheck.viewmodel.UniformCheckViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val uniformCheckPageModule: Module = module {
    viewModel { UniformCheckViewModel(get(), get(), get(), get(), get(), get()) }
}
