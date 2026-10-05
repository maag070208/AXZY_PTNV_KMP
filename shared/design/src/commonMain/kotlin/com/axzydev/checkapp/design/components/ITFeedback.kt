package com.axzydev.checkapp.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Un aviso efímero para el usuario. */
data class FeedbackMessage(
    val text: String,
    val tone: Tone,
)

/**
 * Controlador de feedback.
 *
 * Cualquier acción que muta datos (crear, guardar, resolver, eliminar) debe
 * avisar al usuario: sin esto, tocar "Eliminar" y que la fila desaparezca en
 * silencio no se distingue de que el toque no se registró.
 *
 * Se inyecta una sola instancia en el shell y las pantallas la leen con
 * `LocalFeedback.current`.
 */
class FeedbackController {
    private val _messages = MutableSharedFlow<FeedbackMessage>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    fun show(text: String, tone: Tone = Tone.Brand) {
        _messages.tryEmit(FeedbackMessage(text, tone))
    }

    fun success(text: String) = show(text, Tone.Success)
    fun error(text: String) = show(text, Tone.Danger)
    fun info(text: String) = show(text, Tone.Info)
    /** Acción destructiva confirmada. */
    fun danger(text: String) = show(text, Tone.Danger)
}

/** Acceso al controlador de feedback desde cualquier pantalla. */
val LocalFeedback = staticCompositionLocalOf<FeedbackController?> { null }

/**
 * Anfitrión del feedback: dibuja el contenido y, encima, la pastilla del último
 * aviso. Va en el shell, sobre el área de contenido (por encima de la barra
 * inferior), para que el aviso no la tape.
 */
@Composable
fun ITFeedbackHost(
    controller: FeedbackController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var current by remember { mutableStateOf<FeedbackMessage?>(null) }

    LaunchedEffect(controller) {
        controller.messages.collect { message ->
            current = message
            delay(FEEDBACK_DURATION_MS)
            if (current == message) current = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        content()

        AnimatedVisibility(
            visible = current != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = AxzySpacing.lg, vertical = AxzySpacing.md),
        ) {
            current?.let { FeedbackBar(message = it) }
        }
    }
}

@Composable
private fun FeedbackBar(message: FeedbackMessage) {
    val colors = message.tone.palette

    Surface(
        color = colors.solid,
        shape = AxzyShape.pill,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AxzySpacing.lg, vertical = AxzySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
        ) {
            Icon(
                imageVector = when (message.tone) {
                    Tone.Danger -> ITIcons.Warning
                    Tone.Warning -> ITIcons.Warning
                    else -> ITIcons.ShieldCheck
                },
                contentDescription = null,
                tint = colors.onSolid,
                modifier = Modifier.size(18.dp),
            )
            ITText(
                text = message.text,
                color = colors.onSolid,
                style = AxzyType.cardBody,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private const val FEEDBACK_DURATION_MS = 2600L
