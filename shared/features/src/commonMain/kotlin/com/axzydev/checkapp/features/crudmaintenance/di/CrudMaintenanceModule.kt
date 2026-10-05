package com.axzydev.checkapp.features.crudmaintenance.di

import com.axzydev.checkapp.features.crudmaintenance.model.DeleteMaintenanceUseCase
import com.axzydev.checkapp.features.crudmaintenance.model.ListMaintenancesUseCase
import com.axzydev.checkapp.features.crudmaintenance.model.ResolveMaintenanceUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val crudMaintenanceModule: Module = module {
    factory { ListMaintenancesUseCase(get()) }
    factory { ResolveMaintenanceUseCase(get()) }
    factory { DeleteMaintenanceUseCase(get()) }
}
