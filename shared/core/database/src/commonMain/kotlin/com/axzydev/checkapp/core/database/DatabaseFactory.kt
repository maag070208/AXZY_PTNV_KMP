package com.axzydev.checkapp.core.database

import app.cash.sqldelight.db.SqlDriver

/** Nombre de la base local (paridad con la app RN). */
const val DATABASE_NAME = "AxzyCheckDB.db"

/** Fábrica de drivers por plataforma. */
interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

/**
 * Crea la instancia de la base SQLDelight compartida y expone el driver para
 * el motor de sincronización (que necesita acceso genérico por SQL).
 *
 * Los drivers de plataforma (`AndroidSqliteDriver` / `NativeSqliteDriver`)
 * aplican el esquema y las migraciones automáticamente a partir de
 * `AxzyCheckDatabase.Schema`.
 */
class AxzyCheckDatabaseFactory(driverFactory: DatabaseDriverFactory) {
    /** Driver único de la app. */
    val driver: SqlDriver = driverFactory.createDriver()

    private val database: AxzyCheckDatabase by lazy { AxzyCheckDatabase(driver) }

    fun get(): AxzyCheckDatabase = database
}
