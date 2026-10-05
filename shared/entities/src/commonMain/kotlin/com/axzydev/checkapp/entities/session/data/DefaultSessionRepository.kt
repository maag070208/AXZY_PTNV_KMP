package com.axzydev.checkapp.entities.session.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.core.datastore.SettingsKeys
import com.axzydev.checkapp.core.datastore.SettingsStore
import com.axzydev.checkapp.entities.session.model.Session
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultSessionRepository(
    private val settings: SettingsStore,
    private val authApi: AuthApi,
    private val timeProvider: TimeProvider,
    private val tokenCache: TokenCache,
) : SessionRepository {

    private val _session = MutableStateFlow<Session?>(null)
    override val sessionState: StateFlow<Session?> = _session.asStateFlow()

    override fun currentToken(): String? = tokenCache.token

    override suspend fun restore(): Session? {
        val token = settings.getString(SettingsKeys.AUTH_TOKEN) ?: return null
        val session = Session.fromToken(token)
        if (session == null || session.isExpired(nowEpochSeconds())) {
            clearLocal()
            return null
        }
        apply(session)
        return session
    }

    override suspend fun login(username: String, password: String): ApiResult<Session> {
        return when (val result = authApi.login(username, password)) {
            is ApiResult.Success -> {
                val session = Session.fromToken(result.data)
                    ?: return ApiResult.Failure(listOf("Token de sesión inválido"))
                if (session.isExpired(nowEpochSeconds())) {
                    return ApiResult.Failure(listOf("La sesión recibida ya expiró"))
                }
                persist(session)
                ApiResult.Success(session)
            }

            is ApiResult.Failure -> result
        }
    }

    override suspend fun logout() {
        runCatching { settings.remove(SettingsKeys.AUTH_TOKEN) }
        clearLocal()
    }

    override fun updateDisplayName(fullName: String) {
        val current = _session.value ?: return
        _session.value = current.copy(fullName = fullName)
    }

    private suspend fun persist(session: Session) {
        settings.putString(SettingsKeys.AUTH_TOKEN, session.token)
        apply(session)
    }

    private suspend fun clearLocal() {
        tokenCache.token = null
        _session.value = null
    }

    private fun apply(session: Session) {
        tokenCache.token = session.token
        _session.value = session
    }

    private fun nowEpochSeconds(): Long = timeProvider.nowEpochMillis() / 1000
}
