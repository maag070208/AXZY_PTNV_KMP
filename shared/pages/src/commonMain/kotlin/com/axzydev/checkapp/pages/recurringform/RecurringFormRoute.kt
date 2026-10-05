package com.axzydev.checkapp.pages.recurringform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.recurringform.ui.RecurringFormScreen
import com.axzydev.checkapp.pages.recurringform.viewmodel.RecurringFormEffect
import com.axzydev.checkapp.pages.recurringform.viewmodel.RecurringFormViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecurringFormRoute(
    routeId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RecurringFormViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, routeId) { viewModel.load(routeId) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RecurringFormEffect.Saved -> onSaved()
                is RecurringFormEffect.Error -> Unit
            }
        }
    }

    RecurringFormScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        modifier = modifier,
    )
}
