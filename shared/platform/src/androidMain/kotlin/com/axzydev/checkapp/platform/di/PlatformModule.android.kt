package com.axzydev.checkapp.platform.di

import com.axzydev.checkapp.core.database.AndroidDatabaseDriverFactory
import com.axzydev.checkapp.core.database.DatabaseDriverFactory
import com.axzydev.checkapp.core.datastore.AndroidSettingsStore
import com.axzydev.checkapp.core.datastore.SettingsStore
import com.axzydev.checkapp.core.sync.FileBytesReader
import com.axzydev.checkapp.platform.AndroidPlatformInfo
import com.axzydev.checkapp.platform.PlatformInfo
import com.axzydev.checkapp.platform.fs.OkioFileBytesReader
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(androidContext()) }
    single<SettingsStore> { AndroidSettingsStore(androidContext()) }
    single<PlatformInfo> { AndroidPlatformInfo() }
    single<FileBytesReader> { OkioFileBytesReader() }
}
