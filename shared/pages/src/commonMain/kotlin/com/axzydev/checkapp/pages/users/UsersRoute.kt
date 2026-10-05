package com.axzydev.checkapp.pages.users

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.users.ui.UsersScreen
import com.axzydev.checkapp.pages.users.viewmodel.UsersViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UsersRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: UsersViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) { viewModel.effects.collect { /* errores en estado */ } }

    UsersScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
