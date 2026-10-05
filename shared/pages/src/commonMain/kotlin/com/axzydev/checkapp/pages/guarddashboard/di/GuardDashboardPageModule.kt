package com.axzydev.checkapp.pages.guarddashboard.di

import com.axzydev.checkapp.pages.guarddashboard.viewmodel.GuardDashboardViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val guardDashboardPageModule: Module = module {
    viewModel {
        GuardDashboardViewModel(
            sessionRepository = get(),
            recurringRoutes = get(),
            rounds = get(),
            kardex = get(),
            startRound = get(),
            endRound = get(),
            triggerPanicAlert = get(),
            flushPanicQueue = get(),
        )
    }
}
