package com.axzydev.checkapp.entities.incident.di

import com.axzydev.checkapp.entities.incident.data.DefaultIncidentRepository
import com.axzydev.checkapp.entities.incident.data.IncidentRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val incidentModule: Module = module {
    single<IncidentRepository> { DefaultIncidentRepository(get(), get(), get()) }
}
