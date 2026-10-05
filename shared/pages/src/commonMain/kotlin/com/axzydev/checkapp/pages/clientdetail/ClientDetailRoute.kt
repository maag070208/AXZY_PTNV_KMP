package com.axzydev.checkapp.pages.clientdetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.axzydev.checkapp.pages.clientdetail.ui.ClientDetailScreen

@Composable
fun ClientDetailRoute(
    onOpenZones: () -> Unit,
    onOpenLocations: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ClientDetailScreen(
        onOpenZones = onOpenZones,
        onOpenLocations = onOpenLocations,
        onBack = onBack,
        modifier = modifier,
    )
}
