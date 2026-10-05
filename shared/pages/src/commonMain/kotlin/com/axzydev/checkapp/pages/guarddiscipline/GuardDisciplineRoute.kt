package com.axzydev.checkapp.pages.guarddiscipline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.guarddiscipline.ui.GuardDisciplineScreen
import com.axzydev.checkapp.pages.guarddiscipline.viewmodel.GuardDisciplineEffect
import com.axzydev.checkapp.pages.guarddiscipline.viewmodel.GuardDisciplineViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GuardDisciplineRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GuardDisciplineViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                GuardDisciplineEffect.Resolved -> feedback?.success("Queja resuelta")
                is GuardDisciplineEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    GuardDisciplineScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
