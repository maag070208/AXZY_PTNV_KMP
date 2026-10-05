package com.axzydev.checkapp.pages.guarddetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.guarddetail.ui.GuardDetailScreen
import com.axzydev.checkapp.pages.guarddetail.viewmodel.GuardDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GuardDetailRoute(
    guardId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GuardDetailViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, guardId) { viewModel.load(guardId) }

    GuardDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
