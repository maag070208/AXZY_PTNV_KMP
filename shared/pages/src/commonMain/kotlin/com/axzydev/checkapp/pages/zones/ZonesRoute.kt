package com.axzydev.checkapp.pages.zones

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.zones.ui.ZonesScreen
import com.axzydev.checkapp.pages.zones.viewmodel.ZonesViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ZonesRoute(
    clientId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ZonesViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, clientId) { viewModel.load(clientId) }
    LaunchedEffect(viewModel) { viewModel.effects.collect { /* errores en estado */ } }

    ZonesScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
