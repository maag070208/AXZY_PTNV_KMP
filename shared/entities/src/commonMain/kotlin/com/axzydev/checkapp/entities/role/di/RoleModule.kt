package com.axzydev.checkapp.entities.role.di

import com.axzydev.checkapp.entities.role.data.DefaultRoleRepository
import com.axzydev.checkapp.entities.role.data.RoleRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val roleModule: Module = module {
    single<RoleRepository> { DefaultRoleRepository(get()) }
}
