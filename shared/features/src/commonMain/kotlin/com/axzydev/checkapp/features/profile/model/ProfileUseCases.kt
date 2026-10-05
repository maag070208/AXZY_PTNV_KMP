package com.axzydev.checkapp.features.profile.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.user.data.UserRepository
import com.axzydev.checkapp.entities.user.model.User
import com.axzydev.checkapp.entities.user.model.UserUpdateDraft

/** Lee el usuario en sesión (nombre/apellidos) desde la copia local. */
class GetProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String): User? = repository.findById(id)
}

/** Actualiza el nombre/apellidos del usuario en sesión. */
class UpdateProfileUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String, name: String, lastName: String?): ApiResult<User> =
        repository.update(id, UserUpdateDraft(name = name, lastName = lastName))
}

/** Cambia la contraseña del usuario en sesión (requiere la contraseña anterior). */
class ChangePasswordUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String, oldPassword: String, newPassword: String): ApiResult<Unit> =
        repository.changePassword(id, oldPassword, newPassword)
}
