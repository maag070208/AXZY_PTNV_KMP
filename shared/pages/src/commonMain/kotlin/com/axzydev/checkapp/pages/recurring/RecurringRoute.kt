package com.axzydev.checkapp.pages.recurring

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.recurring.ui.RecurringScreen
import com.axzydev.checkapp.pages.recurring.viewmodel.RecurringEffect
import com.axzydev.checkapp.pages.recurring.viewmodel.RecurringViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecurringRoute(
    onNewRoute: () -> Unit,
    onEditRoute: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RecurringViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RecurringEffect.Deleted -> feedback?.danger("Ruta eliminada")
                is RecurringEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    RecurringScreen(
        state = state,
        onNewRoute = onNewRoute,
        onEditRoute = onEditRoute,
        onAction = viewModel::onAction,
        onBack = onBack,
        modifier = modifier,
    )
}
