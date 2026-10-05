package com.axzydev.checkapp.pages.assignments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.assignments.ui.AssignmentsScreen
import com.axzydev.checkapp.pages.assignments.viewmodel.AssignmentsEffect
import com.axzydev.checkapp.pages.assignments.viewmodel.AssignmentsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AssignmentsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AssignmentsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AssignmentsEffect.Created -> feedback?.success("Asignación creada")
                AssignmentsEffect.Updated -> feedback?.success("Estado actualizado")
                AssignmentsEffect.Deleted -> feedback?.danger("Asignación eliminada")
                is AssignmentsEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    AssignmentsScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
