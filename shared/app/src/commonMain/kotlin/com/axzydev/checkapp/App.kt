package com.axzydev.checkapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.axzydev.checkapp.app.navigation.AppNavigation
import com.axzydev.checkapp.design.theme.AxzyTheme

/**
 * Raíz de la app Compose Multiplatform (Android + iOS).
 * El grafo Koin debe estar iniciado antes de llamar a `App()`.
 *
 * El tema sigue el modo oscuro del sistema.
 */
@Composable
fun App() {
    AxzyTheme(darkTheme = isSystemInDarkTheme()) {
        AppNavigation()
    }
}
