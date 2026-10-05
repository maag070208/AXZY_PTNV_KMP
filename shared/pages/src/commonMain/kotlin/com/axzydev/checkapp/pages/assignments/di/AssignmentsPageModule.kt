package com.axzydev.checkapp.pages.assignments.di

import com.axzydev.checkapp.pages.assignments.viewmodel.AssignmentsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val assignmentsPageModule: Module = module {
    viewModel { AssignmentsViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
