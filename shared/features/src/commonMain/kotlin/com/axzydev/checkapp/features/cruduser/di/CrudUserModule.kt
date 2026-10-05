package com.axzydev.checkapp.features.cruduser.di

import com.axzydev.checkapp.features.cruduser.model.CreateUserUseCase
import com.axzydev.checkapp.features.cruduser.model.DeleteUserUseCase
import com.axzydev.checkapp.features.cruduser.model.ListRolesUseCase
import com.axzydev.checkapp.features.cruduser.model.ListUsersUseCase
import com.axzydev.checkapp.features.cruduser.model.UpdateUserUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val crudUserModule: Module = module {
    factory { ListUsersUseCase(get()) }
    factory { CreateUserUseCase(get()) }
    factory { UpdateUserUseCase(get()) }
    factory { DeleteUserUseCase(get()) }
    factory { ListRolesUseCase(get()) }
}
