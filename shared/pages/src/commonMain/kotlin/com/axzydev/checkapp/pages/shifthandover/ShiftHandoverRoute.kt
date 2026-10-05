package com.axzydev.checkapp.pages.shifthandover

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.shifthandover.ui.ShiftHandoverScreen
import com.axzydev.checkapp.pages.shifthandover.viewmodel.ShiftHandoverEffect
import com.axzydev.checkapp.pages.shifthandover.viewmodel.ShiftHandoverViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShiftHandoverRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ShiftHandoverViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) { viewModel.load() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ShiftHandoverEffect.Saved -> feedback?.success("Entrega de turno registrada")
                is ShiftHandoverEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    ShiftHandoverScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
