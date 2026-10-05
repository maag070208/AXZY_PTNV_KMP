package com.axzydev.checkapp.core.sync.di

import com.axzydev.checkapp.core.network.ApiConfig
import com.axzydev.checkapp.core.sync.DefaultSyncEngine
import com.axzydev.checkapp.core.sync.FileBytesReader
import com.axzydev.checkapp.core.sync.KtorMediaUploader
import com.axzydev.checkapp.core.sync.KtorSyncApi
import com.axzydev.checkapp.core.sync.MediaUploader
import com.axzydev.checkapp.core.sync.SqliteSyncLocalStore
import com.axzydev.checkapp.core.sync.SyncApi
import com.axzydev.checkapp.core.sync.SyncEngine
import com.axzydev.checkapp.core.sync.SyncLocalStore
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Módulo Koin del motor de sync.
 * Requiere que `platformModule()` provea `FileBytesReader`.
 */
val syncModule: Module = module {
    single<SyncApi> { KtorSyncApi(get()) }
    single<SyncLocalStore> { SqliteSyncLocalStore(get()) }
    single<MediaUploader> { KtorMediaUploader(get(), get<ApiConfig>().baseUrl, get<FileBytesReader>()) }
    single<SyncEngine> { DefaultSyncEngine(get(), get(), get(), get()) }
}
