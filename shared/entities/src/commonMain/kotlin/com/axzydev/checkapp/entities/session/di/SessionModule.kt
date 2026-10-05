package com.axzydev.checkapp.entities.session.di

import com.axzydev.checkapp.entities.session.data.AuthApi
import com.axzydev.checkapp.entities.session.data.DefaultSessionRepository
import com.axzydev.checkapp.entities.session.data.KtorAuthApi
import com.axzydev.checkapp.entities.session.data.TokenCache
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/** Módulo Koin de la entidad `session`. */
val sessionModule: Module = module {
    single { TokenCache() }
    single<AuthApi> { KtorAuthApi(get()) }
    single<SessionRepository> {
        DefaultSessionRepository(
            settings = get(),
            authApi = get(),
            timeProvider = get(),
            tokenCache = get(),
        )
    }
}
