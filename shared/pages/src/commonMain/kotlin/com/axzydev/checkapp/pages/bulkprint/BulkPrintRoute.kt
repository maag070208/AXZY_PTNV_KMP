package com.axzydev.checkapp.pages.bulkprint

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.bulkprint.ui.BulkPrintScreen
import com.axzydev.checkapp.pages.bulkprint.viewmodel.BulkPrintAction
import com.axzydev.checkapp.pages.bulkprint.viewmodel.BulkPrintViewModel
import com.axzydev.checkapp.platform.share.rememberSharer
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BulkPrintRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: BulkPrintViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sharer = rememberSharer()

    LaunchedEffect(viewModel) { viewModel.refresh() }

    BulkPrintScreen(
        state = state,
        onAction = viewModel::onAction,
        onShareSheet = {
            val text = state.sheet.joinToString("\n\n") { "${it.name}\n${it.payload}" }
            sharer.shareText(text = text, subject = "Códigos QR de puntos")
        },
        onBack = onBack,
        modifier = modifier,
    )
}
