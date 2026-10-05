package com.axzydev.checkapp.entities.session.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.network.ApiClient
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)

/** Acceso remoto a autenticación. */
interface AuthApi {
    suspend fun login(username: String, password: String): ApiResult<String>
}

class KtorAuthApi(private val client: ApiClient) : AuthApi {
    override suspend fun login(username: String, password: String): ApiResult<String> =
        client.post<String>("/users/login", LoginRequest(username, password))
}
