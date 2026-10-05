package com.axzydev.checkapp.platform.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberSharer(): Sharer = remember { IosSharer() }

@OptIn(ExperimentalForeignApi::class)
private class IosSharer : Sharer {
    override fun shareText(text: String, subject: String?) {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val controller = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        root.presentViewController(controller, animated = true, completion = null)
    }
}
