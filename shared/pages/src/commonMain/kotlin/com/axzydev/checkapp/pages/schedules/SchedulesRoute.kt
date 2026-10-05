package com.axzydev.checkapp.pages.schedules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.schedules.ui.SchedulesScreen
import com.axzydev.checkapp.pages.schedules.viewmodel.SchedulesEffect
import com.axzydev.checkapp.pages.schedules.viewmodel.SchedulesViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SchedulesRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SchedulesViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SchedulesEffect.Created -> feedback?.success("Horario creado")
                SchedulesEffect.Deleted -> feedback?.danger("Horario eliminado")
                is SchedulesEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    SchedulesScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
