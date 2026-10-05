package com.axzydev.checkapp.features.crudincident.di

import com.axzydev.checkapp.features.crudincident.model.DeleteIncidentUseCase
import com.axzydev.checkapp.features.crudincident.model.ListIncidentsUseCase
import com.axzydev.checkapp.features.crudincident.model.ResolveIncidentUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val crudIncidentModule: Module = module {
    factory { ListIncidentsUseCase(get()) }
    factory { ResolveIncidentUseCase(get()) }
    factory { DeleteIncidentUseCase(get()) }
}
