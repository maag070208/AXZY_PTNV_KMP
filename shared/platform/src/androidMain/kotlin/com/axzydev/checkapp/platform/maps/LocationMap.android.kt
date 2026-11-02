package com.axzydev.checkapp.platform.maps

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.axzydev.checkapp.platform.location.Coordinates
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Implementación Android con el SDK de Google Maps.
 *
 * Se usa la `MapView` clásica dentro de `AndroidView` en vez de una envoltura de
 * Compose: son cuatro llamadas de ciclo de vida y así no se ata la versión de Compose
 * del proyecto a la de esa envoltura.
 */
@Composable
actual fun LocationMap(location: Coordinates, modifier: Modifier, dark: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context).apply { onCreate(Bundle()) } }

    // La cámara sólo se mueve cuando la posición cambia de verdad: si se reencuadrara
    // en cada recomposición, el mapa pelearía con quien lo mueve con el dedo.
    val applied = remember { mutableStateOf<Pair<Coordinates, Boolean>?>(null) }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            // La cámara sólo se reencuadra cuando cambia la posición o el tema.
            val state = location to dark
            if (applied.value == state) return@AndroidView
            applied.value = state

            val target = LatLng(location.latitude, location.longitude)
            view.getMapAsync { map ->
                map.uiSettings.isMapToolbarEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                if (dark) {
                    // El estilo del sistema es claro; sin esto el mapa es un cuadro
                    // blanco dentro de una pantalla oscura. El JSON va como constante
                    // y no como recurso: `:shared:platform` es un módulo KMP que no
                    // genera `R`, y un estilo inválido sólo deja el mapa claro —se
                    // degrada, no falla—.
                    map.setMapStyle(MapStyleOptions(DARK_MAP_STYLE))
                }
                map.clear()
                map.addMarker(MarkerOptions().position(target))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(target, DEFAULT_ZOOM))
            }
        },
    )
}

private const val DEFAULT_ZOOM = 17f

/**
 * Estilo oscuro del mapa.
 *
 * Los tonos son los de la paleta en oscuro (slate900 de fondo, slate800 en calles y
 * etiquetas en slate400) para que el mapa se lea como parte de la pantalla y no como
 * una ventana a otra app.
 */
private val DARK_MAP_STYLE = """
    [
      { "elementType": "geometry", "stylers": [{ "color": "#0f172a" }] },
      { "elementType": "labels.text.fill", "stylers": [{ "color": "#94a3b8" }] },
      { "elementType": "labels.text.stroke", "stylers": [{ "color": "#0f172a" }] },
      { "featureType": "administrative", "elementType": "geometry", "stylers": [{ "color": "#334155" }] },
      { "featureType": "poi", "elementType": "geometry", "stylers": [{ "color": "#1e293b" }] },
      { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [{ "color": "#64748b" }] },
      { "featureType": "road", "elementType": "geometry", "stylers": [{ "color": "#334155" }] },
      { "featureType": "road", "elementType": "labels.text.fill", "stylers": [{ "color": "#cbd5e1" }] },
      { "featureType": "road.highway", "elementType": "geometry", "stylers": [{ "color": "#475569" }] },
      { "featureType": "transit", "elementType": "geometry", "stylers": [{ "color": "#1e293b" }] },
      { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#020617" }] },
      { "featureType": "water", "elementType": "labels.text.fill", "stylers": [{ "color": "#475569" }] }
    ]
""".trimIndent()
