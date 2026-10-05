package com.axzydev.checkapp.pages.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import com.axzydev.checkapp.features.profile.model.ChangePasswordUseCase
import com.axzydev.checkapp.features.profile.model.GetProfileUseCase
import com.axzydev.checkapp.features.profile.model.UpdateProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Edición del perfil (nombre/apellidos) y cambio de contraseña del usuario en sesión. */
class ProfileViewModel(
    private val getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val changePassword: ChangePasswordUseCase,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val session = sessionRepository.sessionState.value
            if (session == null) {
                _state.update { it.copy(loading = false, error = "Sesión no disponible") }
                return@launch
            }
            val user = runCatching { getProfile(session.userId) }.getOrNull()
            _state.update {
                it.copy(
                    loading = false,
                    userId = session.userId,
                    username = session.username,
                    name = user?.name ?: session.fullName,
                    lastName = user?.lastName.orEmpty(),
                )
            }
        }
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.Name -> _state.update { it.copy(name = action.value, message = null, error = null) }
            is ProfileAction.LastName -> _state.update { it.copy(lastName = action.value, message = null, error = null) }
            ProfileAction.SaveProfile -> saveProfile()
            is ProfileAction.OldPassword -> _state.update { it.copy(oldPassword = action.value, message = null, error = null) }
            is ProfileAction.NewPassword -> _state.update { it.copy(newPassword = action.value, message = null, error = null) }
            is ProfileAction.ConfirmPassword -> _state.update { it.copy(confirmPassword = action.value, message = null, error = null) }
            ProfileAction.ChangePassword -> change()
            ProfileAction.DismissMessage -> _state.update { it.copy(message = null, error = null) }
        }
    }

    private fun saveProfile() {
        val current = _state.value
        val name = current.name.trim()
        if (name.isBlank()) {
            _state.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        val lastName = current.lastName.trim().ifBlank { null }
        _state.update { it.copy(savingProfile = true, error = null, message = null) }
        viewModelScope.launch {
            when (val result = updateProfile(current.userId, name, lastName)) {
                is ApiResult.Success -> {
                    sessionRepository.updateDisplayName(listOfNotNull(name, lastName).joinToString(" "))
                    _state.update { it.copy(savingProfile = false, message = "Perfil actualizado") }
                }

                is ApiResult.Failure -> _state.update { it.copy(savingProfile = false, error = result.firstMessage) }
            }
        }
    }

    private fun change() {
        val current = _state.value
        val error = when {
            current.oldPassword.isBlank() -> "Escribe tu contraseña actual"
            current.newPassword.length < 6 -> "La nueva contraseña debe tener al menos 6 caracteres"
            current.newPassword != current.confirmPassword -> "La confirmación no coincide"
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        _state.update { it.copy(changingPassword = true, error = null, message = null) }
        viewModelScope.launch {
            when (val result = changePassword(current.userId, current.oldPassword, current.newPassword)) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        changingPassword = false,
                        oldPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        message = "Contraseña actualizada",
                    )
                }

                is ApiResult.Failure -> _state.update { it.copy(changingPassword = false, error = result.firstMessage) }
            }
        }
    }
}
