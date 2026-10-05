package com.axzydev.checkapp.pages.incidents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.incidents.ui.IncidentsScreen
import com.axzydev.checkapp.pages.incidents.viewmodel.IncidentsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun IncidentsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: IncidentsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) { viewModel.effects.collect { /* errores en estado */ } }

    IncidentsScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
