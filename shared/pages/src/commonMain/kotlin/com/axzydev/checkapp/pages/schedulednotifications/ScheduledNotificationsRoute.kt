package com.axzydev.checkapp.pages.schedulednotifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.schedulednotifications.ui.ScheduledNotificationsScreen
import com.axzydev.checkapp.pages.schedulednotifications.viewmodel.ScheduledNotificationsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScheduledNotificationsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ScheduledNotificationsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }

    ScheduledNotificationsScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
