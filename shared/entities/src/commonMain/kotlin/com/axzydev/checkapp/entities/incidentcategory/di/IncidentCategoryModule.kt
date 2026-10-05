package com.axzydev.checkapp.entities.incidentcategory.di

import com.axzydev.checkapp.entities.incidentcategory.data.DefaultIncidentCategoryRepository
import com.axzydev.checkapp.entities.incidentcategory.data.IncidentCategoryRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val incidentCategoryModule: Module = module {
    single<IncidentCategoryRepository> { DefaultIncidentCategoryRepository(get()) }
}
