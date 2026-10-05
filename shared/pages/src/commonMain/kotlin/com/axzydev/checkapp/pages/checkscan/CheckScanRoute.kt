package com.axzydev.checkapp.pages.checkscan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.checkscan.ui.CheckScanScreen
import com.axzydev.checkapp.pages.checkscan.viewmodel.CheckScanEffect
import com.axzydev.checkapp.pages.checkscan.viewmodel.CheckScanViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckScanRoute(
    onLocationSelected: (locationId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CheckScanViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CheckScanEffect.Found -> onLocationSelected(effect.locationId)
                is CheckScanEffect.NotFound -> Unit
            }
        }
    }

    CheckScanScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
