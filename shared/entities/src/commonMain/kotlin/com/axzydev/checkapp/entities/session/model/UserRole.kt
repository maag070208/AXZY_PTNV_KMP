package com.axzydev.checkapp.entities.session.model

/** Roles del sistema (paridad con la app React Native). */
enum class UserRole {
    ADMIN,
    SHIFT,
    GUARD,
    MAINT,
    RESDN;

    val isGuard: Boolean get() = this == GUARD

    companion object {
        fun from(value: String?): UserRole? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
