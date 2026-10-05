package com.axzydev.checkapp.platform.di

import com.axzydev.checkapp.core.database.DatabaseDriverFactory
import com.axzydev.checkapp.core.database.IosDatabaseDriverFactory
import com.axzydev.checkapp.core.datastore.IosSettingsStore
import com.axzydev.checkapp.core.datastore.SettingsStore
import com.axzydev.checkapp.core.sync.FileBytesReader
import com.axzydev.checkapp.platform.IosPlatformInfo
import com.axzydev.checkapp.platform.PlatformInfo
import com.axzydev.checkapp.platform.fs.OkioFileBytesReader
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<DatabaseDriverFactory> { IosDatabaseDriverFactory() }
    single<SettingsStore> { IosSettingsStore() }
    single<PlatformInfo> { IosPlatformInfo() }
    single<FileBytesReader> { OkioFileBytesReader() }
}
