package com.axzydev.checkapp.pages.clientdetail.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSectionTitle
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.icons.ITIcons

/**
 * Detalle de un cliente.
 *
 * Es un menú de sus dos colecciones: zonas y puntos de control. Se usan tarjetas
 * pulsables en lugar de botones apilados porque cada una lleva a un listado, no
 * ejecuta una acción.
 */
@Composable
fun ClientDetailScreen(
    onOpenZones: () -> Unit,
    onOpenLocations: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ITScreenScaffold(
        title = "Cliente",
        subtitle = "Zonas y puntos de control",
        state = ScreenState.Ready,
        modifier = modifier,
        onBack = onBack,
    ) {
        ITSectionTitle(text = "Catálogo del cliente")

        Spacer(Modifier.height(AxzySpacing.xs))

        ITListItem(
            title = "Zonas",
            subtitle = "Agrupaciones dentro del cliente",
            avatarInitial = "Z",
            avatarStatus = Tone.Info,
            onClick = onOpenZones,
        )

        ITListItem(
            title = "Puntos de control",
            subtitle = "Los QRs que escanea el guardia",
            avatarInitial = "P",
            avatarStatus = Tone.Success,
            onClick = onOpenLocations,
        )

        Spacer(Modifier.height(AxzySpacing.lg))

        ITListItem(
            title = "Guardias asignados",
            subtitle = "Todavía no disponible en la app",
            avatarInitial = "G",
            avatarStatus = Tone.Neutral,
        )
    }
}
