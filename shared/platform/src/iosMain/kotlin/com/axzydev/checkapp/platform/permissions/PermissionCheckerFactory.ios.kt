package com.axzydev.checkapp.platform.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.axzydev.checkapp.core.permissions.AppPermission
import com.axzydev.checkapp.core.permissions.PermissionChecker
import com.axzydev.checkapp.core.permissions.PermissionStatus
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaType
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.Foundation.NSURL
import platform.Foundation.NSURL.Companion.URLWithString
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Implementación iOS.
 *
 * Cada permiso lo lleva un framework distinto —cámara y micrófono por AVFoundation,
 * ubicación por CoreLocation— y ninguno tiene consulta de "ya no preguntes": iOS sólo
 * distingue *no determinado* de *denegado*, así que un denegado es `BLOCKED` (el
 * sistema no vuelve a mostrar el diálogo).
 */
@Composable
actual fun rememberPermissionChecker(): PermissionChecker = remember { IosPermissionChecker() }

@OptIn(ExperimentalForeignApi::class)
private class IosPermissionChecker : PermissionChecker {

    private val location = LocationPermission()

    override suspend fun status(permission: AppPermission): PermissionStatus = when (permission) {
        // iOS no pide permiso para usar la red.
        AppPermission.INTERNET -> PermissionStatus.GRANTED
        AppPermission.CAMERA -> avStatus(AVMediaTypeVideo)
        AppPermission.MICROPHONE -> avStatus(AVMediaTypeAudio)
        AppPermission.LOCATION -> location.currentStatus()
    }

    override suspend fun request(permission: AppPermission): PermissionStatus = when (permission) {
        AppPermission.INTERNET -> PermissionStatus.GRANTED
        AppPermission.CAMERA -> requestAv(AVMediaTypeVideo)
        AppPermission.MICROPHONE -> requestAv(AVMediaTypeAudio)
        AppPermission.LOCATION -> location.request()
    }

    override fun openSettings() {
        val url = URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
    }

    private suspend fun requestAv(mediaType: AVMediaType): PermissionStatus =
        suspendCancellableCoroutine { continuation ->
            AVCaptureDevice.requestAccessForMediaType(mediaType) { granted ->
                // El callback no llega en el hilo principal y aquí se toca estado de
                // Compose: se devuelve antes de continuar.
                dispatch_async(dispatch_get_main_queue()) {
                    continuation.resume(if (granted) PermissionStatus.GRANTED else avStatus(mediaType))
                }
            }
        }
}

/**
 * Estado de un permiso de AVFoundation.
 *
 * `Restricted` —control parental, política del aparato— se trata como bloqueado:
 * tampoco se puede conceder desde una solicitud.
 */
@OptIn(ExperimentalForeignApi::class)
private fun avStatus(mediaType: AVMediaType): PermissionStatus =
    when (AVCaptureDevice.authorizationStatusForMediaType(mediaType)) {
        AVAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
        AVAuthorizationStatusNotDetermined -> PermissionStatus.DENIED
        else -> PermissionStatus.BLOCKED
    }

/**
 * Solicitud de ubicación.
 *
 * Se queda con el `CLLocationManager` vivo: la respuesta llega por delegado y, si el
 * manager se libera antes, no llega nunca.
 */
@OptIn(ExperimentalForeignApi::class)
private class LocationPermission : NSObject(), CLLocationManagerDelegateProtocol {

    private val manager = CLLocationManager()
    private var continuation: kotlinx.coroutines.CancellableContinuation<PermissionStatus>? = null

    init {
        manager.delegate = this
    }

    fun currentStatus(): PermissionStatus = manager.authorizationStatus.toPermissionStatus()

    suspend fun request(): PermissionStatus = suspendCancellableCoroutine { continuation ->
        this.continuation = continuation
        continuation.invokeOnCancellation { this.continuation = null }
        manager.requestWhenInUseAuthorization()
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        val status = manager.authorizationStatus
        // Sin decidir todavía: el sistema avisa al crear el manager y al resolver.
        if (status == kCLAuthorizationStatusNotDetermined) return
        continuation?.resume(status.toPermissionStatus())
        continuation = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CLAuthorizationStatus.toPermissionStatus(): PermissionStatus = when (this) {
    kCLAuthorizationStatusAuthorizedAlways,
    kCLAuthorizationStatusAuthorizedWhenInUse,
    -> PermissionStatus.GRANTED

    kCLAuthorizationStatusNotDetermined -> PermissionStatus.DENIED

    // Denegada o restringida: el diálogo no vuelve a salir.
    else -> PermissionStatus.BLOCKED
}
