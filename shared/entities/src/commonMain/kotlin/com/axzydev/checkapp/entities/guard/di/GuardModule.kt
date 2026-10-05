package com.axzydev.checkapp.entities.guard.di

import com.axzydev.checkapp.entities.guard.data.DefaultGuardRepository
import com.axzydev.checkapp.entities.guard.data.GuardRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val guardModule: Module = module {
    single<GuardRepository> { DefaultGuardRepository(get(), get()) }
}
