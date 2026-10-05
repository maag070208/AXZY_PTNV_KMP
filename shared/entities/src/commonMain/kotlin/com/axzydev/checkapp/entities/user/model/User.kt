package com.axzydev.checkapp.entities.user.model

data class User(
    val id: String,
    val name: String,
    val lastName: String?,
    val username: String,
    val active: Boolean,
    val roleId: String?,
    val roleName: String?,
    val clientId: String?,
    val scheduleId: String?,
)

data class UserDraft(
    val name: String,
    val username: String,
    val password: String,
    val roleId: String,
    val lastName: String? = null,
    val clientId: String? = null,
    val scheduleId: String? = null,
)

/** Datos editables de un usuario (sin credenciales). */
data class UserUpdateDraft(
    val name: String,
    val lastName: String? = null,
    val roleId: String? = null,
    val clientId: String? = null,
)
