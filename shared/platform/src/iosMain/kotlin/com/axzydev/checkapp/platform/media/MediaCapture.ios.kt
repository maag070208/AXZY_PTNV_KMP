package com.axzydev.checkapp.platform.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerMediaURL
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

/**
 * Implementación iOS: presenta `UIImagePickerController` con la cámara y guarda
 * la evidencia capturada en el directorio temporal de la app.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberMediaCapture(): MediaCaptureLauncher {
    val launcher = remember { IosMediaCaptureLauncher() }
    DisposableEffect(Unit) {
        onDispose { launcher.dismiss() }
    }
    return launcher
}

@OptIn(ExperimentalForeignApi::class)
private class IosMediaCaptureLauncher : MediaCaptureLauncher {

    override val supportsVideo: Boolean = true

    private val delegate = PickerDelegate()
    private var pending: ((CaptureResult) -> Unit)? = null
    private var pendingIsVideo: Boolean = false
    private var picker: UIImagePickerController? = null

    init {
        delegate.onFinish = { info -> handleFinish(info) }
        delegate.onCancel = { handleCancel() }
    }

    override fun capturePhoto(onResult: (CaptureResult) -> Unit) = present(isVideo = false, onResult = onResult)

    override fun captureVideo(onResult: (CaptureResult) -> Unit) = present(isVideo = true, onResult = onResult)

    private fun present(isVideo: Boolean, onResult: (CaptureResult) -> Unit) {
        if (!UIImagePickerController.isSourceTypeAvailable(
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
            )
        ) {
            onResult(CaptureResult.Failed("La cámara no está disponible en este dispositivo"))
            return
        }
        pending = onResult
        pendingIsVideo = isVideo

        val controller = UIImagePickerController().apply {
            sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            mediaTypes = listOf(if (isVideo) "public.movie" else "public.image")
            delegate = this@IosMediaCaptureLauncher.delegate
        }
        picker = controller

        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (root == null) {
            pending = null
            picker = null
            onResult(CaptureResult.Failed("No se pudo abrir la cámara"))
        } else {
            root.presentViewController(controller, animated = true, completion = null)
        }
    }

    private fun handleFinish(info: Map<Any?, *>) {
        picker?.dismissViewControllerAnimated(true, completion = null)
        picker = null
        val callback = pending ?: return
        pending = null

        val result = if (pendingIsVideo) {
            val url = info[UIImagePickerControllerMediaURL] as? NSURL
            val path = url?.path
            if (path != null) CaptureResult.Captured(path, isVideo = true) else CaptureResult.Cancelled
        } else {
            val image = info[UIImagePickerControllerOriginalImage] as? UIImage
            val path = image?.let { writeData(UIImageJPEGRepresentation(it, 0.9), "jpg") }
            if (path != null) CaptureResult.Captured(path, isVideo = false) else CaptureResult.Cancelled
        }
        callback(result)
    }

    private fun handleCancel() {
        picker?.dismissViewControllerAnimated(true, completion = null)
        picker = null
        pending?.invoke(CaptureResult.Cancelled)
        pending = null
    }

    fun dismiss() {
        picker?.dismissViewControllerAnimated(true, completion = null)
        picker = null
        pending = null
    }

    private fun writeData(data: NSData?, extension: String): String? {
        val bytes = data ?: return null
        val path = "${NSTemporaryDirectory()}evidence_${NSDate().timeIntervalSince1970}.$extension"
        return if (bytes.writeToFile(path, atomically = true)) path else null
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PickerDelegate :
    NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {

    var onFinish: ((Map<Any?, *>) -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        onFinish?.invoke(didFinishPickingMediaWithInfo)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onCancel?.invoke()
    }
}
