package com.axzydev.checkapp.pages.maintenance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.maintenance.ui.MaintenanceScreen
import com.axzydev.checkapp.pages.maintenance.viewmodel.MaintenanceViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MaintenanceRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MaintenanceViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) { viewModel.effects.collect { /* errores en estado */ } }

    MaintenanceScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
