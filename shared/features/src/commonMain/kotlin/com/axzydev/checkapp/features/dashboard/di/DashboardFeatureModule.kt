package com.axzydev.checkapp.features.dashboard.di

import com.axzydev.checkapp.features.dashboard.model.GetDashboardStatsUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val dashboardFeatureModule: Module = module {
    factory { GetDashboardStatsUseCase(get()) }
}
