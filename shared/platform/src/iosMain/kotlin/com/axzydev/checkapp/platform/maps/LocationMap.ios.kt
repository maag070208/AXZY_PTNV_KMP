package com.axzydev.checkapp.platform.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.axzydev.checkapp.platform.location.Coordinates

/**
 * Mapa en iOS: pendiente.
 *
 * La ubicación **sí** se captura en iOS (`CLLocationManager`), lo que falta es el
 * mapa. Se declara aquí en vez de dejar el `actual` sin implementar para que el
 * proyecto compile y para que quien lo vea en pantalla sepa que es una ausencia
 * conocida y no una falla.
 */
@Composable
actual fun LocationMap(location: Coordinates, modifier: Modifier, dark: Boolean) {
    Box(
        modifier = modifier.fillMaxSize().background(PENDING_BACKGROUND),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "El mapa llega en la próxima versión de iOS",
            color = PENDING_TEXT,
            textAlign = TextAlign.Center,
        )
    }
}

private val PENDING_BACKGROUND = Color(0xFFE8ECEF)
private val PENDING_TEXT = Color(0xFF64748B)
