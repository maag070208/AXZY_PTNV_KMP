package com.axzydev.checkapp.pages.assignments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.assignments.ui.AssignmentsScreen
import com.axzydev.checkapp.pages.assignments.viewmodel.AssignmentsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AssignmentsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AssignmentsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) { viewModel.effects.collect { /* errores en estado */ } }

    AssignmentsScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
