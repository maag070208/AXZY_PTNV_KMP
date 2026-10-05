package com.axzydev.checkapp.entities.dashboard.di

import com.axzydev.checkapp.entities.dashboard.data.DashboardRepository
import com.axzydev.checkapp.entities.dashboard.data.DefaultDashboardRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val dashboardModule: Module = module {
    single<DashboardRepository> { DefaultDashboardRepository(get()) }
}
