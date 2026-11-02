package com.axzydev.checkapp.platform.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.resume
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject

/**
 * Implementación iOS sobre CoreLocation.
 *
 * `requestLocation` entrega **una** posición y se detiene solo, que es justo lo que
 * hace falta aquí. Si no llega —sin señal, bajo techo— el delegado avisa del error y
 * se devuelve `null`: la marcación sigue su curso sin coordenadas.
 */
@Composable
actual fun rememberLocationProvider(): LocationProvider = remember { IosLocationProvider() }

private class IosLocationProvider : LocationProvider {

    private val requester = LocationRequester()

    override suspend fun current(): Coordinates? = requester.request()
}

/**
 * El delegado va aparte del proveedor: Kotlin/Native **no permite** que una clase
 * herede de `NSObject` e implemente a la vez una interfaz de Kotlin.
 */
@OptIn(ExperimentalForeignApi::class)
private class LocationRequester : NSObject(), CLLocationManagerDelegateProtocol {

    private val manager = CLLocationManager()

    private var continuation: kotlinx.coroutines.CancellableContinuation<Coordinates?>? = null

    init {
        manager.delegate = this
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }

    suspend fun request(): Coordinates? {
        val status = manager.authorizationStatus
        val authorized = status == kCLAuthorizationStatusAuthorizedAlways ||
            status == kCLAuthorizationStatusAuthorizedWhenInUse

        // Sin permiso o con los servicios apagados no hay nada que preguntar.
        if (!authorized || !CLLocationManager.locationServicesEnabled()) return null

        return suspendCancellableCoroutine { continuation ->
            this.continuation = continuation
            continuation.invokeOnCancellation {
                this.continuation = null
                manager.stopUpdatingLocation()
            }
            manager.requestLocation()
        }
    }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        continuation?.resume(location?.toCoordinates())
        continuation = null
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        continuation?.resume(null)
        continuation = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CLLocation.toCoordinates(): Coordinates {
    val center = coordinate.useContents { latitude to longitude }
    return Coordinates(
        latitude = center.first,
        longitude = center.second,
        accuracyMeters = horizontalAccuracy.takeIf { it > 0 }?.toFloat(),
    )
}
