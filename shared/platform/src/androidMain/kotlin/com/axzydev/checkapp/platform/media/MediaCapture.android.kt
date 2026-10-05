package com.axzydev.checkapp.platform.media

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

/**
 * Implementación Android: usa los contratos de captura del sistema
 * (`ACTION_IMAGE_CAPTURE` / `ACTION_VIDEO_CAPTURE`) apuntando a un archivo
 * temporal del propio cache expuesto vía `FileProvider`.
 */
@Composable
actual fun rememberMediaCapture(): MediaCaptureLauncher {
    val context = LocalContext.current
    val pendingPhoto = remember { mutableStateOf<((CaptureResult) -> Unit)?>(null) }
    val pendingVideo = remember { mutableStateOf<((CaptureResult) -> Unit)?>(null) }
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val videoUri = remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = photoUri.value
        val callback = pendingPhoto.value
        pendingPhoto.value = null
        photoUri.value = null
        callback?.invoke(
            if (success && uri != null) {
                CaptureResult.Captured(uri.toString(), isVideo = false)
            } else {
                CaptureResult.Cancelled
            },
        )
    }

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        val uri = videoUri.value
        val callback = pendingVideo.value
        pendingVideo.value = null
        videoUri.value = null
        callback?.invoke(
            if (success && uri != null) {
                CaptureResult.Captured(uri.toString(), isVideo = true)
            } else {
                CaptureResult.Cancelled
            },
        )
    }

    return remember(context, photoLauncher, videoLauncher) {
        object : MediaCaptureLauncher {
            override val supportsVideo: Boolean = true

            override fun capturePhoto(onResult: (CaptureResult) -> Unit) {
                launch(pendingPhoto, onResult) {
                    val uri = createOutputUri(context, "jpg")
                    photoUri.value = uri
                    photoLauncher.launch(uri)
                }
            }

            override fun captureVideo(onResult: (CaptureResult) -> Unit) {
                launch(pendingVideo, onResult) {
                    val uri = createOutputUri(context, "mp4")
                    videoUri.value = uri
                    videoLauncher.launch(uri)
                }
            }

            private fun launch(
                pending: androidx.compose.runtime.MutableState<((CaptureResult) -> Unit)?>,
                onResult: (CaptureResult) -> Unit,
                block: () -> Unit,
            ) {
                pending.value = onResult
                runCatching(block).onFailure { error ->
                    pending.value = null
                    onResult(CaptureResult.Failed(error.message ?: "No se pudo abrir la cámara"))
                }
            }
        }
    }
}

private fun createOutputUri(context: Context, extension: String): Uri {
    val directory = File(context.cacheDir, "captures").apply { mkdirs() }
    val file = File(directory, "capture_${System.currentTimeMillis()}.$extension")
    file.createNewFile()
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
