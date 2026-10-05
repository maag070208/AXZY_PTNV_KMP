package com.axzydev.checkapp.entities.location.di

import com.axzydev.checkapp.entities.location.data.DefaultLocationRepository
import com.axzydev.checkapp.entities.location.data.LocationRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val locationModule: Module = module {
    single<LocationRepository> { DefaultLocationRepository(get(), get()) }
}
