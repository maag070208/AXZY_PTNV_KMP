package com.axzydev.checkapp.design.components

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Acción para abrir el menú lateral, publicada por el shell de la app.
 *
 * El shell (capa `app`) es el único que conoce el drawer. Sin esto habría que
 * pasar un `onOpenMenu` por las 30 rutas y sus 30 pantallas, y cada pantalla
 * nueva tendría que acordarse de aceptarlo. Publicándolo aquí, cualquier
 * `ITScreenScaffold` muestra el botón de menú por el simple hecho de estar
 * dentro del shell.
 *
 * Es `null` fuera del shell (por ejemplo en la pantalla de login), y en ese caso
 * el botón no se pinta.
 */
val LocalOnOpenDrawer = staticCompositionLocalOf<(() -> Unit)?> { null }
