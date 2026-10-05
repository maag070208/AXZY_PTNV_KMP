package com.axzydev.checkapp.pages.schedules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.schedules.ui.SchedulesScreen
import com.axzydev.checkapp.pages.schedules.viewmodel.SchedulesViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SchedulesRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SchedulesViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) { viewModel.effects.collect { /* errores en estado */ } }

    SchedulesScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
