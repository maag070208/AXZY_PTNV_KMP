package com.axzydev.checkapp.entities.synccatalog.di

import com.axzydev.checkapp.entities.synccatalog.data.DefaultSyncCatalogRepository
import com.axzydev.checkapp.entities.synccatalog.data.SyncCatalogRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val syncCatalogModule: Module = module {
    single<SyncCatalogRepository> { DefaultSyncCatalogRepository(get()) }
}
