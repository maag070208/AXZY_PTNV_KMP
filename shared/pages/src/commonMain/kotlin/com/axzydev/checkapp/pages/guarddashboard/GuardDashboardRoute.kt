package com.axzydev.checkapp.pages.guarddashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.guarddashboard.ui.GuardDashboardScreen
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.GuardDashboardEffect
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.GuardDashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GuardDashboardRoute(
    onOpenCheck: (roundId: String, locationId: String) -> Unit,
    onScan: (roundId: String) -> Unit,
    onOpenRound: (roundId: String) -> Unit,
    onOpenKardex: () -> Unit,
    onReportIncident: () -> Unit,
    onReportMaintenance: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSync: () -> Unit,
    onLogout: () -> Unit,
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
                is GuardDashboardEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    GuardDashboardScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenCheck = onOpenCheck,
        onScan = { state.activeRoundId?.let(onScan) },
        onOpenRound = onOpenRound,
        onOpenKardex = onOpenKardex,
        onReportIncident = onReportIncident,
        onReportMaintenance = onReportMaintenance,
        onOpenProfile = onOpenProfile,
        onOpenNotifications = onOpenNotifications,
        onSync = onSync,
        onLogout = onLogout,
        modifier = modifier,
    )
}
