package com.axzydev.checkapp.pages.guarddashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.guarddashboard.ui.GuardDashboardScreen
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.GuardDashboardAction
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.GuardDashboardEffect
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.GuardDashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GuardDashboardRoute(
    onOpenCheck: (roundId: String, locationId: String) -> Unit,
    onReportIncident: () -> Unit,
    onReportMaintenance: () -> Unit,
    onSync: () -> Unit,
    /**
     * Abrir el menú lateral.
     *
     * Faltaba: la hamburguesa del panel del guardia no hacía nada porque la
     * pantalla se quedaba con el `onOpenMenu` por defecto, que está vacío.
     */
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GuardDashboardViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }
    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                GuardDashboardEffect.RoundFinished -> feedback?.success("Ruta finalizada")
                is GuardDashboardEffect.OpenCheck -> {
                    state.activeRoundId?.let { roundId -> onOpenCheck(roundId, effect.locationId) }
                }
                is GuardDashboardEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
    GuardDashboardScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenCheck = onOpenCheck,
        onScan = { viewModel.onAction(GuardDashboardAction.OpenScanner) },
        onQrCode = { code -> viewModel.onAction(GuardDashboardAction.Scanned(code)) },
        onReportIncident = onReportIncident,
        onReportMaintenance = onReportMaintenance,
        onSync = onSync,
        onOpenMenu = onOpenMenu,
    )
    }
}
