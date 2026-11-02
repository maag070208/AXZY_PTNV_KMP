package com.axzydev.checkapp.core.permissions

/**
 * Permiso de plataforma que la app puede llegar a necesitar.
 *
 * @param runtime `false` para los que el sistema concede al instalar y no se pueden
 *   pedir ni revocar (internet). Se listan para poder mostrarlos, pero sin botón:
 *   ofrecer una acción que el sistema no admite es peor que no ofrecerla.
 */
enum class AppPermission(val runtime: Boolean) {
    INTERNET(runtime = false),
    LOCATION(runtime = true),
    CAMERA(runtime = true),
    MICROPHONE(runtime = true),
}

/**
 * Estado de un permiso tal y como lo ve el sistema.
 *
 * `BLOCKED` es el caso que importa: el usuario dijo que no dos veces (o marcó "no
 * volver a preguntar") y **el diálogo del sistema ya no aparece**. Sin distinguirlo
 * de `DENIED`, la interfaz ofrecería un botón que no hace nada.
 */
enum class PermissionStatus {
    GRANTED,
    DENIED,
    BLOCKED,

    /** El aparato no tiene el hardware, o la plataforma no pide ese permiso. */
    UNAVAILABLE,
}

/**
 * ¿Hay que hacer algo con este permiso?
 *
 * `UNAVAILABLE` **no** cuenta: el aparato no lo tiene y no hay nada que conceder.
 * Sin esa distinción, un equipo sin cámara pediría permisos en cada entrada.
 */
val PermissionStatus.needsAttention: Boolean
    get() = this == PermissionStatus.DENIED || this == PermissionStatus.BLOCKED

/**
 * Consulta y solicitud de permisos del sistema.
 *
 * La implementación **no puede ser un objeto de DI**: pedir un permiso necesita una
 * actividad viva. Se obtiene con `rememberPermissionChecker()` desde la composición
 * (en `:shared:platform`, que es donde vive el `actual`).
 *
 * `request` es `suspend` y devuelve el estado resultante, no un `Boolean`: la
 * respuesta llega cuando el usuario decide, y de paso se sabe si quedó bloqueado.
 */
interface PermissionChecker {
    suspend fun status(permission: AppPermission): PermissionStatus
    suspend fun request(permission: AppPermission): PermissionStatus

    /** Ajustes de la app: es la única salida cuando el permiso quedó bloqueado. */
    fun openSettings()
}

/** Implementación de prueba / previews: dice que sí (o que no) a todo. */
class GrantedPermissionChecker(private val granted: Boolean = true) : PermissionChecker {
    override suspend fun status(permission: AppPermission): PermissionStatus =
        if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED

    override suspend fun request(permission: AppPermission): PermissionStatus = status(permission)

    override fun openSettings() = Unit
}
