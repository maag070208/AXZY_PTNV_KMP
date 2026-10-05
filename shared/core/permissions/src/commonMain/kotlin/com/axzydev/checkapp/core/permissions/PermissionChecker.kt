package com.axzydev.checkapp.core.permissions

/**
 * Superficie pública del slice `core:permissions`.
 *
 * Abstrae los permisos de plataforma (cámara, ubicación, notificaciones).
 * La implementación real vive en `:shared:platform`.
 */
enum class AppPermission {
    CAMERA,
    LOCATION,
    NOTIFICATIONS,
}

interface PermissionChecker {
    suspend fun isGranted(permission: AppPermission): Boolean
    suspend fun request(permission: AppPermission): Boolean
}

/** Implementación de prueba / previews. */
class GrantedPermissionChecker(private val granted: Boolean = true) : PermissionChecker {
    override suspend fun isGranted(permission: AppPermission): Boolean = granted
    override suspend fun request(permission: AppPermission): Boolean = granted
}
