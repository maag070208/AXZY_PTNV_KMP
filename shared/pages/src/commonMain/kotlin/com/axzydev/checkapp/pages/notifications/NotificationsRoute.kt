package com.axzydev.checkapp.pages.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.notifications.ui.NotificationsScreen
import com.axzydev.checkapp.pages.notifications.viewmodel.NotificationsEffect
import com.axzydev.checkapp.pages.notifications.viewmodel.NotificationsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotificationsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: NotificationsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                NotificationsEffect.MarkedRead -> feedback?.success("Notificación marcada como leída")
                NotificationsEffect.MarkedAllRead -> feedback?.success("Todas marcadas como leídas")
                is NotificationsEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    NotificationsScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
