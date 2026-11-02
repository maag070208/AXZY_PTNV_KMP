package com.axzydev.checkapp.platform.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File

/**
 * Implementación Android.
 *
 * La foto se toma con **CameraX dentro de la app** (`MediaCaptureHost`): sólo encuadre
 * y disparador. El vídeo sigue usando el contrato del sistema —`ACTION_VIDEO_CAPTURE`—
 * porque grabar y codificar es otra conversación; si algún día se pide igual de simple,
 * el camino es `VideoCapture` de CameraX.
 */
@Composable
actual fun rememberMediaCapture(): MediaCaptureLauncher {
    val context = LocalContext.current
    val pendingPhoto = remember { mutableStateOf<((CaptureResult) -> Unit)?>(null) }
    val pendingVideo = remember { mutableStateOf<((CaptureResult) -> Unit)?>(null) }

    return remember(context) {
        AndroidMediaCapture(
            context = context,
            pendingPhoto = pendingPhoto,
            pendingVideo = pendingVideo,
        )
    }
}

private class AndroidMediaCapture(
    private val context: Context,
    val pendingPhoto: androidx.compose.runtime.MutableState<((CaptureResult) -> Unit)?>,
    val pendingVideo: androidx.compose.runtime.MutableState<((CaptureResult) -> Unit)?>,
) : MediaCaptureLauncher {

    override val supportsVideo: Boolean = true

    /** Deja la captura pendiente: la pinta [MediaCaptureHost]. */
    override fun capturePhoto(onResult: (CaptureResult) -> Unit) {
        pendingPhoto.value = onResult
    }

    /** Igual que la foto: se graba dentro de la app, no en la del sistema. */
    override fun captureVideo(onResult: (CaptureResult) -> Unit) {
        pendingVideo.value = onResult
    }

    fun finishVideo(result: CaptureResult) {
        val callback = pendingVideo.value
        pendingVideo.value = null
        callback?.invoke(result)
    }

    fun finish(result: CaptureResult) {
        val callback = pendingPhoto.value
        pendingPhoto.value = null
        callback?.invoke(result)
    }

    fun cancel() = finish(CaptureResult.Cancelled)
}

/**
 * Cámara de la app mientras hay una foto pendiente.
 *
 * Sólo tiene encuadre y disparador: ni zoom, ni volteo, ni ajustes. Es lo que se hace
 * aquí —encuadrar un punto y disparar— y todo lo demás es ruido en el momento en que
 * menos tiempo hay.
 */
@Composable
actual fun MediaCaptureHost(launcher: MediaCaptureLauncher) {
    val capture = launcher as? AndroidMediaCapture ?: return
    val videoMode = capture.pendingVideo.value != null
    if (capture.pendingPhoto.value == null && !videoMode) return

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { result ->
        granted = result
        // Sin permiso no hay nada que encuadrar: se cierra y se dice por qué.
        if (!result) capture.finish(CaptureResult.Failed("Sin permiso de cámara"))
    }

    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val videoCapture = remember {
        VideoCapture.withOutput(
            Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HD)).build(),
        )
    }
    var recording by remember { mutableStateOf<Recording?>(null) }
    val file = remember { createOutputFile(context, if (videoMode) "mp4" else "jpg") }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (granted) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener(
                        {
                            // Sin `runCatching` mudo: si el enlace falla, el visor queda
                            // negro y el guardia no sabe si esperar o salir. Se cierra
                            // la captura con el motivo.
                            runCatching {
                                val provider = providerFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    if (videoMode) videoCapture else imageCapture,
                                )
                            }.onFailure { error ->
                                capture.finish(
                                    CaptureResult.Failed(
                                        error.message ?: "No se pudo abrir la cámara",
                                    ),
                                )
                            }
                        },
                        ContextCompat.getMainExecutor(ctx),
                    )
                    previewView
                },
            )
        } else {
            Text(
                text = "Se necesita el permiso de cámara para la evidencia",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = when {
                    !videoMode -> "Encuadra el punto"
                    recording != null -> "Grabando…"
                    else -> "Graba el vídeo del punto"
                },
                color = Color.White,
            )

            // Disparador: aro blanco con relleno. Es el único control.
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
                    .padding(8.dp)
                    .background(if (recording != null) Color(0xFFE11D48) else Color.White, CircleShape)
                    .clickable {
                        if (videoMode) {
                            val active = recording
                            if (active == null) {
                                val options = FileOutputOptions.Builder(file).build()
                                recording = videoCapture.output
                                    .prepareRecording(context, options)
                                    .withAudioEnabled()
                                    .start(ContextCompat.getMainExecutor(context)) { event ->
                                        if (event is VideoRecordEvent.Finalize) {
                                            recording = null
                                            capture.finishVideo(
                                                if (event.hasError()) {
                                                    CaptureResult.Failed("No se pudo grabar el vídeo")
                                                } else {
                                                    CaptureResult.Captured(
                                                        uri = Uri.fromFile(file).toString(),
                                                        isVideo = true,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                return@clickable
                            }

                            active.stop()
                            return@clickable
                        }

                        val options = ImageCapture.OutputFileOptions.Builder(file).build()

                        imageCapture.takePicture(
                            options,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    capture.finish(
                                        // `file://` y no `content://`: el archivo es
                                        // nuestro y el lector de bytes —el que sube la
                                        // evidencia— sólo entiende rutas. El
                                        // FileProvider existía para prestarle el
                                        // archivo a la app de cámara del sistema, que
                                        // ya no se usa.
                                        CaptureResult.Captured(
                                            uri = Uri.fromFile(file).toString(),
                                            isVideo = false,
                                        ),
                                    )
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    capture.finish(
                                        CaptureResult.Failed(
                                            exception.message ?: "No se pudo tomar la foto",
                                        ),
                                    )
                                }
                            },
                        )
                    },
            )
        }

        // Salir sin foto: un texto discreto arriba, para no dejar la pantalla sin
        // salida más que el gesto del sistema.
        Text(
            text = "Cancelar",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(24.dp)
                .clickable { capture.cancel() },
        )
    }

    DisposableEffect(Unit) {
        onDispose { capture.pendingPhoto.value = null }
    }
}

private fun createOutputFile(context: Context, extension: String): File {
    val directory = File(context.cacheDir, "captures").apply { mkdirs() }
    return File(directory, "capture_${System.currentTimeMillis()}.$extension")
}
