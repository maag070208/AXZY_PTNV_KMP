package com.axzydev.checkapp.pages.uniformcheck

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.uniformcheck.ui.UniformCheckScreen
import com.axzydev.checkapp.pages.uniformcheck.viewmodel.UniformCheckViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UniformCheckRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: UniformCheckViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.load() }
    LaunchedEffect(viewModel) { viewModel.effects.collect { /* errores en estado */ } }

    UniformCheckScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
