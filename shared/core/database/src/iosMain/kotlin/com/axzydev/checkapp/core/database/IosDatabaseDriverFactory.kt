package com.axzydev.checkapp.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver =
        NativeSqliteDriver(AxzyCheckDatabase.Schema, DATABASE_NAME)
}
