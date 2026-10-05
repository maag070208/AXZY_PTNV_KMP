package com.axzydev.checkapp.pages.clients

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.clients.ui.ClientsScreen
import com.axzydev.checkapp.pages.clients.viewmodel.ClientsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClientsRoute(
    onOpenClient: (clientId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ClientsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) { viewModel.effects.collect { /* errores en estado */ } }

    ClientsScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenClient = onOpenClient,
        onBack = onBack,
        modifier = modifier,
    )
}
