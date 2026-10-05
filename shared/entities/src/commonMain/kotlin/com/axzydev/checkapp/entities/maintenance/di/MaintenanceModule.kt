package com.axzydev.checkapp.entities.maintenance.di

import com.axzydev.checkapp.entities.maintenance.data.DefaultMaintenanceRepository
import com.axzydev.checkapp.entities.maintenance.data.MaintenanceRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val maintenanceModule: Module = module {
    single<MaintenanceRepository> { DefaultMaintenanceRepository(get(), get(), get()) }
}
