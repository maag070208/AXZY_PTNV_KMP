package com.axzydev.checkapp.pages.recurringform.di

import com.axzydev.checkapp.pages.recurringform.viewmodel.RecurringFormViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val recurringFormPageModule: Module = module {
    viewModel {
        RecurringFormViewModel(
            listClients = get(),
            listZones = get(),
            listLocations = get(),
            listGuards = get(),
            getRecurring = get(),
            createRecurring = get(),
            updateRecurring = get(),
        )
    }
}
