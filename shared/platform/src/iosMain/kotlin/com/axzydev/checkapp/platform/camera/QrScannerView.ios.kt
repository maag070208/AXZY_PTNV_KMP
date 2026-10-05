package com.axzydev.checkapp.platform.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Implementación iOS: `AVCaptureSession` con `AVCaptureMetadataOutput` (QR) y una
 * `AVCaptureVideoPreviewLayer` embebida vía `UIKitView`.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QrScannerView(
    onQrCode: (String) -> Unit,
    modifier: Modifier,
) {
    val currentOnQrCode by rememberUpdatedState(onQrCode)

    var granted by remember {
        mutableStateOf(
            AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) ==
                AVAuthorizationStatusAuthorized,
        )
    }
    LaunchedEffect(Unit) {
        if (!granted) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { result ->
                dispatch_async(dispatch_get_main_queue()) { granted = result }
            }
        }
    }

    if (!granted) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Se requiere permiso de cámara para escanear el código QR")
        }
        return
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val session = AVCaptureSession()
            val delegate = QrMetadataDelegate { code -> currentOnQrCode(code) }

            AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)?.let { device ->
                val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
                if (input != null && session.canAddInput(input)) {
                    session.addInput(input)
                }
            }

            val metadataOutput = AVCaptureMetadataOutput()
            if (session.canAddOutput(metadataOutput)) {
                session.addOutput(metadataOutput)
                metadataOutput.setMetadataObjectsDelegate(delegate, dispatch_get_main_queue())
                metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
            }

            dispatch_async(dispatch_get_main_queue()) { session.startRunning() }

            CameraPreviewView(session = session, metadataDelegate = delegate)
        },
        onRelease = { view -> view.stop() },
    )
}

@OptIn(ExperimentalForeignApi::class)
private class CameraPreviewView(
    private val session: AVCaptureSession,
    // Referencia fuerte: `AVCaptureMetadataOutput` solo retiene weak al delegate.
    private val metadataDelegate: QrMetadataDelegate,
) : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {

    private val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        previewLayer.setFrame(bounds)
    }

    fun stop() {
        if (session.running) session.stopRunning()
        metadataDelegate.detach()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class QrMetadataDelegate(
    private var onQrCode: (String) -> Unit,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        val code = didOutputMetadataObjects
            .firstOrNull() as? AVMetadataMachineReadableCodeObject
        val value = code?.stringValue ?: return
        onQrCode(value)
    }

    fun detach() {
        onQrCode = {}
    }
}
