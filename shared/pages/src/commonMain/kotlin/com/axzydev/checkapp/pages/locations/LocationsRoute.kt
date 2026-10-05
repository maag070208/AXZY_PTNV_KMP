package com.axzydev.checkapp.pages.locations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.locations.ui.LocationsScreen
import com.axzydev.checkapp.pages.locations.viewmodel.LocationsEffect
import com.axzydev.checkapp.pages.locations.viewmodel.LocationsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LocationsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LocationsViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel) { viewModel.refresh() }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LocationsEffect.Created -> feedback?.success("Punto creado")
                LocationsEffect.Deleted -> feedback?.danger("Punto eliminado")
                is LocationsEffect.Error -> feedback?.error(effect.message)
            }
        }
    }

    LocationsScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
