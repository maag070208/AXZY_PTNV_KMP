package com.axzydev.checkapp.pages.schedulednotifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.schedulednotifications.ui.ScheduledNotificationsScreen
import com.axzydev.checkapp.pages.schedulednotifications.viewmodel.ScheduledEffect
import com.axzydev.checkapp.pages.schedulednotifications.viewmodel.ScheduledNotificationsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScheduledNotificationsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ScheduledNotificationsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ScheduledEffect.Created -> feedback?.success("Aviso programado creado")
                ScheduledEffect.Toggled -> feedback?.success("Estado del aviso actualizado")
                ScheduledEffect.Deleted -> feedback?.danger("Aviso eliminado")
                is ScheduledEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    ScheduledNotificationsScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
