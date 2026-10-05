package com.axzydev.checkapp.features.authlogin.di

import com.axzydev.checkapp.features.authlogin.model.LoginUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/** Módulo Koin del feature `auth-login`. */
val loginModule: Module = module {
    factory { LoginUseCase(get()) }
}
