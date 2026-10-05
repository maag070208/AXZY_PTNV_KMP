package com.axzydev.checkapp.platform.share

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberSharer(): Sharer {
    val context = LocalContext.current
    return remember(context) {
        object : Sharer {
            override fun shareText(text: String, subject: String?) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(intent, subject ?: "Compartir"))
            }
        }
    }
}
