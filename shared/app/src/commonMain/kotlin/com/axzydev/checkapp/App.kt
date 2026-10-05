package com.axzydev.checkapp

import androidx.compose.runtime.Composable
import com.axzydev.checkapp.app.navigation.AppNavigation
import com.axzydev.checkapp.design.theme.AxzyTheme

/**
 * Raíz de la app Compose Multiplatform (Android + iOS).
 * El grafo Koin debe estar iniciado antes de llamar a `App()`.
 */
@Composable
fun App() {
    AxzyTheme {
        AppNavigation()
    }
}
