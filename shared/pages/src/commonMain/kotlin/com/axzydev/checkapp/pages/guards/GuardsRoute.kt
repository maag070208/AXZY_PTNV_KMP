package com.axzydev.checkapp.pages.guards

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.guards.ui.GuardsScreen
import com.axzydev.checkapp.pages.guards.viewmodel.GuardsEffect
import com.axzydev.checkapp.pages.guards.viewmodel.GuardsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GuardsRoute(
    onOpenGuard: (guardId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GuardsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                GuardsEffect.Deleted -> feedback?.danger("Guardia eliminado")
                is GuardsEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    GuardsScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenGuard = onOpenGuard,
        onBack = onBack,
        modifier = modifier,
    )
}
