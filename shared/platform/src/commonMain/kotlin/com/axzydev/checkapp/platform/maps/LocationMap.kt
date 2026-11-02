package com.axzydev.checkapp.platform.maps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axzydev.checkapp.platform.location.Coordinates

/**
 * Mapa con la posición marcada.
 *
 * Pinta **sólo** el mapa: el hueco de "sin ubicación" lo decide la pantalla, que es
 * quien conoce el Design System. Aquí no se importa a propósito — `:shared:platform`
 * no depende de `:shared:design` y no debe empezar a hacerlo por un estado vacío.
 *
 * Android usa el SDK de Google Maps (la key se inyecta desde `local.properties`);
 * iOS queda pendiente y lo dice en su propio `actual`.
 */
@Composable
expect fun LocationMap(
    location: Coordinates,
    modifier: Modifier = Modifier,
    /** Tema oscuro: el mapa trae su propio estilo claro y deslumbra en una app oscura. */
    dark: Boolean = false,
)
