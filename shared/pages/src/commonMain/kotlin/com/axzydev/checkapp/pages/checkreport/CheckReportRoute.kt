package com.axzydev.checkapp.pages.checkreport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.checkreport.ui.CheckReportScreen
import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportAction
import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportEffect
import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportViewModel
import com.axzydev.checkapp.platform.media.CaptureResult
import com.axzydev.checkapp.platform.media.rememberMediaCapture
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckReportRoute(
    roundId: String,
    locationId: String,
    onSubmitted: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CheckReportViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mediaCapture = rememberMediaCapture()

    LaunchedEffect(viewModel, roundId, locationId) { viewModel.load(roundId, locationId) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CheckReportEffect.Submitted -> onSubmitted()
                is CheckReportEffect.Error -> Unit
            }
        }
    }

    CheckReportScreen(
        state = state,
        supportsVideo = mediaCapture.supportsVideo,
        onCapturePhoto = {
            mediaCapture.capturePhoto { result -> result.forwardTo(viewModel) }
        },
        onCaptureVideo = {
            mediaCapture.captureVideo { result -> result.forwardTo(viewModel) }
        },
        onAction = viewModel::onAction,
        onBack = onBack,
        modifier = modifier,
    )
}

private fun CaptureResult.forwardTo(viewModel: CheckReportViewModel) {
    when (this) {
        is CaptureResult.Captured ->
            viewModel.onAction(CheckReportAction.AddMedia(uri, isVideo))

        is CaptureResult.Failed ->
            viewModel.onAction(CheckReportAction.MediaError(message))

        CaptureResult.Cancelled -> Unit
    }
}
