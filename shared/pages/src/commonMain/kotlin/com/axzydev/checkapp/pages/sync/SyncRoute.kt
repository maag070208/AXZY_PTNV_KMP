package com.axzydev.checkapp.pages.sync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.sync.ui.SyncScreen
import com.axzydev.checkapp.pages.sync.viewmodel.SyncEffect
import com.axzydev.checkapp.pages.sync.viewmodel.SyncViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SyncRoute(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SyncViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.start() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SyncEffect.Completed -> onDone()
            }
        }
    }

    SyncScreen(state = state, onAction = viewModel::onAction, onDone = onDone, modifier = modifier)
}
