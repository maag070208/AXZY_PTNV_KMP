package com.axzydev.checkapp.pages.getstarted

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axzydev.checkapp.pages.getstarted.ui.GetStartedScreen

/**
 * Bienvenida.
 *
 * No tiene ViewModel ni módulo de DI **a propósito**: no hay estado que sostener ni
 * nada que cargar, sólo una acción que resuelve la navegación. Un `Contract` +
 * `ViewModel` aquí sería ceremonia sin contenido.
 *
 * @param returningUserName nombre de quien ya tenía sesión. Con él la pantalla
 *   saluda por su nombre y su botón dice "Continuar"; vacío es una primera vez.
 */
@Composable
fun GetStartedRoute(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    returningUserName: String = "",
) {
    GetStartedScreen(
        onStart = onStart,
        modifier = modifier,
        returningUserName = returningUserName,
    )
}
