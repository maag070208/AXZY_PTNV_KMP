package com.axzydev.checkapp.pages.kardex

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.kardex.ui.KardexScreen
import com.axzydev.checkapp.pages.kardex.viewmodel.KardexViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun KardexRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: KardexViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.refresh() }

    KardexScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
