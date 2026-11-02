package com.axzydev.checkapp.platform.permissions

import androidx.compose.runtime.Composable
import com.axzydev.checkapp.core.permissions.PermissionChecker

/**
 * Implementación de `PermissionChecker` para la plataforma actual.
 *
 * Se recuerda en composición **a propósito**: pedir un permiso necesita una actividad
 * viva, así que no puede ser un objeto suelto de inyección. Es la misma forma que
 * `rememberMediaCapture`.
 */
@Composable
expect fun rememberPermissionChecker(): PermissionChecker
