package com.axzydev.checkapp.pages.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.profile.ui.ProfileScreen
import com.axzydev.checkapp.pages.profile.viewmodel.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProfileViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) { viewModel.load() }

    ProfileScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
