package com.axzydev.checkapp.entities.zone.di

import com.axzydev.checkapp.entities.zone.data.DefaultZoneRepository
import com.axzydev.checkapp.entities.zone.data.ZoneRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val zoneModule: Module = module {
    single<ZoneRepository> { DefaultZoneRepository(get(), get()) }
}
