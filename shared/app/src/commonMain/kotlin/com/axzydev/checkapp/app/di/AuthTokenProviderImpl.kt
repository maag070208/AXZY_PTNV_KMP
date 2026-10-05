package com.axzydev.checkapp.app.di

import com.axzydev.checkapp.core.network.AuthTokenProvider
import com.axzydev.checkapp.entities.session.data.TokenCache

/** Lee el token desde la caché en memoria (sin depender del repositorio → sin ciclo). */
class AuthTokenProviderImpl(private val tokenCache: TokenCache) : AuthTokenProvider {
    override fun token(): String? = tokenCache.token
}
