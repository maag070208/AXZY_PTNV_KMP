package com.axzydev.checkapp.pages.maintenance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.maintenance.ui.MaintenanceScreen
import com.axzydev.checkapp.pages.maintenance.viewmodel.MaintenanceEffect
import com.axzydev.checkapp.pages.maintenance.viewmodel.MaintenanceViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MaintenanceRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MaintenanceViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                MaintenanceEffect.Resolved -> feedback?.success("Mantenimiento resuelto")
                MaintenanceEffect.Deleted -> feedback?.danger("Mantenimiento eliminado")
                is MaintenanceEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    MaintenanceScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
