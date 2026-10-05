package com.axzydev.checkapp.pages.profile.viewmodel

data class ProfileUiState(
    val loading: Boolean = true,
    val userId: String = "",
    val username: String = "",
    val name: String = "",
    val lastName: String = "",
    val savingProfile: Boolean = false,
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val changingPassword: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

sealed interface ProfileAction {
    data class Name(val value: String) : ProfileAction
    data class LastName(val value: String) : ProfileAction
    data object SaveProfile : ProfileAction
    data class OldPassword(val value: String) : ProfileAction
    data class NewPassword(val value: String) : ProfileAction
    data class ConfirmPassword(val value: String) : ProfileAction
    data object ChangePassword : ProfileAction
    data object DismissMessage : ProfileAction
}
