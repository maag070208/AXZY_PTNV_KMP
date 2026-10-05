package com.axzydev.checkapp.pages.sendnotification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.sendnotification.ui.SendNotificationScreen
import com.axzydev.checkapp.pages.sendnotification.viewmodel.SendNotificationViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SendNotificationRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SendNotificationViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SendNotificationScreen(state = state, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}
