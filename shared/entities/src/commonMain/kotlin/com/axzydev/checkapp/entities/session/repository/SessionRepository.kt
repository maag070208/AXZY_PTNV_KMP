package com.axzydev.checkapp.entities.session.repository

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.session.model.Session
import kotlinx.coroutines.flow.StateFlow

/**
 * Estado de sesión de la app: persiste el token, lo restaura al arrancar,
 * valida expiración y expone el usuario actual.
 */
interface SessionRepository {
    val sessionState: StateFlow<Session?>

    /** Token en caché para el header Authorization (síncrono). */
    fun currentToken(): String?

    /** Restaura la sesión persistida. `null` si no hay o ya expiró. */
    suspend fun restore(): Session?

    suspend fun login(username: String, password: String): ApiResult<Session>

    /** Actualiza el nombre mostrado en la sesión (tras editar el perfil). */
    fun updateDisplayName(fullName: String)

    suspend fun logout()
}
