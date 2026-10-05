package com.axzydev.checkapp.entities.role.data

import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.entities.role.model.Role

interface RoleRepository {
    suspend fun localAll(): List<Role>
}

class DefaultRoleRepository(private val database: AxzyCheckDatabase) : RoleRepository {
    override suspend fun localAll(): List<Role> =
        database.roleQueries.selectAllRoles().executeAsList().map { Role(it.id, it.name, it.value_) }
}
