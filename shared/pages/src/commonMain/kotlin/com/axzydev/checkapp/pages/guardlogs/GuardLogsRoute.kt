package com.axzydev.checkapp.pages.guardlogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.guardlogs.ui.GuardLogsScreen
import com.axzydev.checkapp.pages.guardlogs.viewmodel.GuardLogsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GuardLogsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GuardLogsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }

    GuardLogsScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
