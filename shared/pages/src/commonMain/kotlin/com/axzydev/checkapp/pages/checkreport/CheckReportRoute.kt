package com.axzydev.checkapp.pages.checkreport

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.axzydev.checkapp.design.components.LocalFeedback
import com.axzydev.checkapp.pages.checkreport.ui.CheckReportScreen
import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportAction
import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportEffect
import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportViewModel
import com.axzydev.checkapp.platform.media.CaptureResult
import com.axzydev.checkapp.platform.location.rememberLocationProvider
import com.axzydev.checkapp.platform.media.MediaCaptureHost
import com.axzydev.checkapp.platform.media.rememberMediaCapture
import kotlinx.coroutines.launch
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
    val locationProvider = rememberLocationProvider()
    val scope = rememberCoroutineScope()

    // La posición se busca al abrir y se puede refrescar: en un punto de control el
    // guardia puede tardar, y guardar dónde estaba hace veinte minutos no es
    // constancia de nada.
    suspend fun locate() {
        viewModel.onAction(CheckReportAction.LocationResolved(locationProvider.current()))
    }

    LaunchedEffect(locationProvider) { locate() }

    val feedback = LocalFeedback.current
    LaunchedEffect(viewModel, roundId, locationId) { viewModel.load(roundId, locationId) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CheckReportEffect.Submitted -> {
                    feedback?.success("Reporte enviado")
                    onSubmitted()
                }
                is CheckReportEffect.Error -> feedback?.error(effect.message)
            }
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        CheckReportScreen(
            state = state,
            supportsVideo = mediaCapture.supportsVideo,
            onCapturePhoto = {
                mediaCapture.capturePhoto { result -> result.forwardTo(viewModel) }
            },
            onCaptureVideo = {
                mediaCapture.captureVideo { result -> result.forwardTo(viewModel) }
            },
            onRefreshLocation = { scope.launch { locate() } },
            onAction = viewModel::onAction,
            onBack = onBack,
            modifier = modifier,
        )
        MediaCaptureHost(mediaCapture)
    }
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
