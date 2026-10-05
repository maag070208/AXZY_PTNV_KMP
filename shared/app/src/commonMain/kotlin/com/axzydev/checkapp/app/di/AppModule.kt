package com.axzydev.checkapp.app.di

import app.cash.sqldelight.db.SqlDriver
import com.axzydev.checkapp.app.config.ApiConstants
import com.axzydev.checkapp.core.common.time.SystemTimeProvider
import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.core.connectivity.ConnectivityObserver
import com.axzydev.checkapp.core.connectivity.FixedConnectivity
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.AxzyCheckDatabaseFactory
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.core.network.ApiConfig
import com.axzydev.checkapp.core.network.AppVersionProvider
import com.axzydev.checkapp.core.network.AuthTokenProvider
import com.axzydev.checkapp.core.network.createHttpClient
import com.axzydev.checkapp.core.permissions.GrantedPermissionChecker
import com.axzydev.checkapp.core.permissions.PermissionChecker
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Grafo de infraestructura de la app. Las implementaciones de plataforma
 * (drivers, settings, etc.) llegan vía `platformModule()`.
 */
val appModule: Module = module {
    single<TimeProvider> { SystemTimeProvider }

    single {
        ApiConfig(
            baseUrl = ApiConstants.BASE_URL,
            defaultAppVersion = ApiConstants.DEFAULT_APP_VERSION,
        )
    }

    single { AppVersionProviderImpl(get()) }
    single<AppVersionProvider> { get<AppVersionProviderImpl>() }
    single<AuthTokenProvider> { AuthTokenProviderImpl(get()) }

    single { createHttpClient(get(), get(), get()) }
    single { ApiClient(get(), get<ApiConfig>().baseUrl) }

    // Base de datos local (SQLDelight)
    single { AxzyCheckDatabaseFactory(get()) }
    single<AxzyCheckDatabase> { get<AxzyCheckDatabaseFactory>().get() }
    single<SqlDriver> { get<AxzyCheckDatabaseFactory>().driver }

    // Implementaciones reales de conectividad/permisos llegan en fases posteriores.
    single<ConnectivityObserver> { FixedConnectivity() }
    single<PermissionChecker> { GrantedPermissionChecker() }
}
