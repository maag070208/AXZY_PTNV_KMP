package com.axzydev.checkapp.features.authlogin.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.session.model.Session
import com.axzydev.checkapp.entities.session.repository.SessionRepository

/** Valida y ejecuta el inicio de sesión. */
class LoginUseCase(private val repository: SessionRepository) {

    suspend operator fun invoke(username: String, password: String): ApiResult<Session> {
        val user = username.trim()
        if (user.isEmpty() || password.isEmpty()) {
            return ApiResult.Failure(listOf("Usuario y contraseña son obligatorios"))
        }
        return repository.login(user, password)
    }
}
