package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType

/**
 * Estado vacío y estado de error.
 *
 * Una lista sin resultados y una petición fallida se veían igual: nada. Eso
 * hace que el usuario no sepa si la app está cargando, vacía o rota. Este
 * componente obliga a decir cuál de las tres cosas es, y ofrece la acción
 * cuando existe.
 *
 * Se pinta sobre una tarjeta blanca para que, centrado en una lista vacía, se
 * lea como un bloque intencional y no como una pantalla a medio construir.
 */
@Composable
fun ITEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AxzySpacing.screenH),
        contentAlignment = Alignment.Center,
    ) {
        ITCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AxzySpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(AxzyColors.surfaceVariant, AxzyShape.pill),
                    contentAlignment = Alignment.Center,
                ) {
                    icon?.invoke() ?: ITText(
                        text = "—",
                        color = AxzyColors.onSurfaceVariant,
                        style = AxzyType.screenTitle,
                    )
                }

                ITText(
                    text = title,
                    color = AxzyColors.onSurface,
                    style = AxzyType.cardTitle,
                    textAlign = TextAlign.Center,
                )

                if (description != null) {
                    ITText(
                        text = description,
                        color = AxzyColors.onSurfaceVariant,
                        style = AxzyType.muted,
                        textAlign = TextAlign.Center,
                    )
                }

                if (actionLabel != null && onAction != null) {
                    ITButton(
                        label = actionLabel,
                        onClick = onAction,
                        modifier = Modifier.padding(top = AxzySpacing.sm),
                        fullWidth = false,
                        compact = true,
                    )
                }
            }
        }
    }
}
