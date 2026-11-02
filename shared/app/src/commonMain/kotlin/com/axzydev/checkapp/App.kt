package com.axzydev.checkapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.axzydev.checkapp.app.navigation.AppNavigation
import com.axzydev.checkapp.core.datastore.ThemeMode
import com.axzydev.checkapp.core.datastore.ThemePreference
import com.axzydev.checkapp.design.theme.AxzyTheme
import org.koin.compose.koinInject

/**
 * Raíz de la app Compose Multiplatform (Android + iOS).
 * El grafo Koin debe estar iniciado antes de llamar a `App()`.
 *
 * El tema sigue el modo oscuro del sistema **hasta que el usuario elige otro** en
 * "Mi perfil". El de fábrica es el del teléfono: la app no impone su gusto.
 */
@Composable
fun App() {
    val themePreference: ThemePreference = koinInject()
    val themeMode by themePreference.mode.collectAsState()

    // Lo guardado se lee al arrancar; mientras tanto manda el sistema, que es el
    // valor por defecto. El splash da de sobra para que no se vea el cambio.
    LaunchedEffect(themePreference) { themePreference.load() }

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    AxzyTheme(darkTheme = darkTheme) {
        AppNavigation()
    }
}
