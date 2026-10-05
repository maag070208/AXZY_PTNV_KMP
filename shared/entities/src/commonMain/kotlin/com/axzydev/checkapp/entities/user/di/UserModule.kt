package com.axzydev.checkapp.entities.user.di

import com.axzydev.checkapp.entities.user.data.DefaultUserRepository
import com.axzydev.checkapp.entities.user.data.UserRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val userModule: Module = module {
    single<UserRepository> { DefaultUserRepository(get(), get()) }
}
