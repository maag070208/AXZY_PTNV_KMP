package com.axzydev.checkapp.platform.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.axzydev.checkapp.core.permissions.AppPermission
import com.axzydev.checkapp.core.permissions.PermissionChecker
import com.axzydev.checkapp.core.permissions.PermissionStatus
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Implementación Android sobre `checkSelfPermission` y el lanzador de permisos.
 *
 * Distinguir `BLOCKED` de `DENIED` es lo único con truco: el sistema no tiene una
 * consulta para "ya no preguntes". El patrón es `shouldShowRequestPermissionRationale`
 * —que devuelve `false` tanto si nunca se pidió como si quedó bloqueado— más recordar
 * aquí si ya se pidió. Sin esa memoria, la interfaz ofrecería un botón muerto.
 */
@Composable
actual fun rememberPermissionChecker(): PermissionChecker {
    val context = LocalContext.current
    val pending = remember { PendingRequest() }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        // No se usa el mapa de resultados: el estado real se relee del sistema, que
        // es la única fuente que también cubre "concedido sólo la aproximada".
        val permission = pending.permission
        pending.complete(
            if (permission == null) {
                PermissionStatus.DENIED
            } else {
                readStatus(context, permission, pending.asked)
            },
        )
    }

    return remember(context, launcher, pending) {
        AndroidPermissionChecker(
            context = context,
            pending = pending,
            launch = { names -> launcher.launch(names) },
        )
    }
}

/**
 * Puente entre el lanzador (que responde por callback) y `request` (que es `suspend`).
 *
 * Sólo puede haber una solicitud viva: el diálogo del sistema es modal.
 */
private class PendingRequest {
    val asked = mutableSetOf<AppPermission>()
    var permission: AppPermission? = null
        private set

    private var continuation: kotlinx.coroutines.CancellableContinuation<PermissionStatus>? = null

    fun begin(
        permission: AppPermission,
        continuation: kotlinx.coroutines.CancellableContinuation<PermissionStatus>,
    ) {
        this.permission = permission
        this.continuation = continuation
        continuation.invokeOnCancellation {
            this.permission = null
            this.continuation = null
        }
    }

    fun complete(status: PermissionStatus) {
        continuation?.resume(status)
        permission = null
        continuation = null
    }
}

private class AndroidPermissionChecker(
    private val context: Context,
    private val pending: PendingRequest,
    private val launch: (Array<String>) -> Unit,
) : PermissionChecker {

    override suspend fun status(permission: AppPermission): PermissionStatus =
        readStatus(context, permission, pending.asked)

    override suspend fun request(permission: AppPermission): PermissionStatus {
        val names = permission.androidNames()
        // Los que el sistema concede al instalar no se piden: se responden.
        if (names.isEmpty()) return status(permission)

        pending.asked += permission

        return suspendCancellableCoroutine { continuation ->
            pending.begin(permission, continuation)
            launch(names)
        }
    }

    override fun openSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

/**
 * Estado real del permiso.
 *
 * @param asked permisos ya pedidos en esta sesión: con eso, un `false` de
 *   `shouldShowRequestPermissionRationale` deja de ser ambiguo.
 */
private fun readStatus(
    context: Context,
    permission: AppPermission,
    asked: Set<AppPermission>,
): PermissionStatus {
    if (!isAvailable(context, permission)) return PermissionStatus.UNAVAILABLE

    val names = permission.androidNames()
    if (names.isEmpty()) return PermissionStatus.GRANTED

    if (names.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
        return PermissionStatus.GRANTED
    }

    val activity = context.findActivity() ?: return PermissionStatus.DENIED
    val canAskAgain = names.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }

    return when {
        canAskAgain -> PermissionStatus.DENIED
        permission in asked -> PermissionStatus.BLOCKED
        else -> PermissionStatus.DENIED
    }
}

/**
 * Permisos de Android de cada uno.
 *
 * Ubicación pide **fina y aproximada**: desde Android 12 el usuario elige, y pedir
 * sólo la fina hace que el sistema ignore la solicitud. La comprobación de concedido
 * usa `any`, así que quien dé la aproximada también cuenta como concedida.
 */
private fun AppPermission.androidNames(): Array<String> = when (this) {
    AppPermission.INTERNET -> emptyArray()
    AppPermission.LOCATION -> arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    AppPermission.CAMERA -> arrayOf(Manifest.permission.CAMERA)
    AppPermission.MICROPHONE -> arrayOf(Manifest.permission.RECORD_AUDIO)
}

/** ¿El aparato tiene el hardware? Ofertar la cámara en un equipo sin cámara confunde. */
private fun isAvailable(context: Context, permission: AppPermission): Boolean = when (permission) {
    AppPermission.CAMERA -> context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    AppPermission.MICROPHONE -> context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    else -> true
}

/** La actividad vive dentro de la cadena de `ContextWrapper` que da Compose. */
private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
