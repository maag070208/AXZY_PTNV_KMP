package com.axzydev.checkapp.features.syncdatabase.di

import com.axzydev.checkapp.features.syncdatabase.model.SyncDatabaseUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val syncFeatureModule: Module = module {
    factory { SyncDatabaseUseCase(get()) }
}
