package com.axzydev.checkapp.platform.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Implementación Android sobre `LocationManager`.
 *
 * Se usa el framework y no Play Services: para **una** posición puntual no compensa
 * sumar un servicio entero, y `getCurrentLocation` —API 30— ya hace lo que hacía
 * Fused para este caso. En equipos más viejos se cae a la última conocida.
 */
@Composable
actual fun rememberLocationProvider(): LocationProvider {
    val context = LocalContext.current
    return remember(context) { AndroidLocationProvider(context) }
}

private class AndroidLocationProvider(private val context: Context) : LocationProvider {

    override suspend fun current(): Coordinates? {
        if (!hasPermission()) return null

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        val provider = enabledProvider(manager) ?: return null

        // Primero una posición fresca, con tope de tiempo: si el aparato está bajo
        // techo, esperar deja al guardia mirando una pantalla quieta. Si no llega,
        // la última conocida sirve —y su precisión lo dice—.
        val fresh = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            withTimeoutOrNull(FRESH_LOCATION_TIMEOUT_MS) { fresh(manager, provider) }
        } else {
            null
        }

        return (fresh ?: lastKnown(manager))?.toCoordinates()
    }

    private fun hasPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    private fun enabledProvider(manager: LocationManager): String? = when {
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> null
    }

    /** La última de cualquier proveedor: la de red suele existir aunque no haya GPS. */
    private fun lastKnown(manager: LocationManager): Location? = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
    ).mapNotNull { provider ->
        runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
    }.maxByOrNull { it.time }

    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun fresh(manager: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            runCatching {
                manager.getCurrentLocation(provider, signal, context.mainExecutor) { location ->
                    continuation.resume(location)
                }
            }.onFailure { continuation.resume(null) }
        }
}

private fun Location.toCoordinates(): Coordinates = Coordinates(
    latitude = latitude,
    longitude = longitude,
    // `accuracy` devuelve 0 cuando no la hay: se prefiere "no sé" a "0 metros".
    accuracyMeters = accuracy.takeIf { hasAccuracy() },
)

/** Tope de espera de una posición fresca. */
private const val FRESH_LOCATION_TIMEOUT_MS = 6_000L
