package com.axzydev.checkapp.pages.reportissue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.pages.reportissue.ui.ReportIssueScreen
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueAction
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueEffect
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueKind
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueViewModel
import com.axzydev.checkapp.platform.media.CaptureResult
import com.axzydev.checkapp.platform.media.rememberMediaCapture
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReportIssueRoute(
    kind: ReportIssueKind,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReportIssueViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mediaCapture = rememberMediaCapture()

    LaunchedEffect(viewModel, kind) { viewModel.load(kind) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ReportIssueEffect.Saved -> onSaved()
                is ReportIssueEffect.Error -> Unit
            }
        }
    }

    ReportIssueScreen(
        state = state,
        supportsVideo = mediaCapture.supportsVideo,
        onCapturePhoto = { mediaCapture.capturePhoto { result -> result.forwardTo(viewModel) } },
        onCaptureVideo = { mediaCapture.captureVideo { result -> result.forwardTo(viewModel) } },
        onAction = viewModel::onAction,
        onBack = onBack,
        modifier = modifier,
    )
}

private fun CaptureResult.forwardTo(viewModel: ReportIssueViewModel) {
    when (this) {
        is CaptureResult.Captured -> viewModel.onAction(ReportIssueAction.AddMedia(uri, isVideo))
        is CaptureResult.Failed -> viewModel.onAction(ReportIssueAction.MediaError(message))
        CaptureResult.Cancelled -> Unit
    }
}
