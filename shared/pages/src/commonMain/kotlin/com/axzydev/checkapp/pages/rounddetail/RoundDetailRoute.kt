package com.axzydev.checkapp.pages.rounddetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.rounddetail.ui.RoundDetailScreen
import com.axzydev.checkapp.pages.rounddetail.viewmodel.RoundDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RoundDetailRoute(
    roundId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RoundDetailViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, roundId) { viewModel.load(roundId) }

    RoundDetailScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
