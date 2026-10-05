package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/** Un punto de una lista de verificación. */
data class ITChecklistItem(
    val key: String,
    val label: String,
    val ok: Boolean,
)

/**
 * Lista de verificación.
 *
 * La usan la revisión de uniforme y la entrega de turno, así que vive en el
 * sistema y no duplicada.
 *
 * Cada fila alterna entre conforme y no conforme **al tocarla**, en lugar de usar
 * casillas o un desplegable: en campo se marca de pie y con una mano, y una fila
 * grande perdona el toque. El estado se distingue por color **y** por icono, para
 * que se lea también sin distinguir colores.
 */
@Composable
fun ITChecklist(
    items: List<ITChecklistItem>,
    onToggle: (key: String, ok: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
    ) {
        items.forEach { item ->
            ChecklistRow(item = item, onToggle = { onToggle(item.key, !item.ok) })
        }
    }
}

@Composable
private fun ChecklistRow(item: ITChecklistItem, onToggle: () -> Unit) {
    val tone = if (item.ok) Tone.Success else Tone.Danger
    val colors = tone.palette

    ITTouchableOpacity(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        scaleTo = 0.98f,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.soft, AxzyShape.md)
                .border(1.dp, colors.border, AxzyShape.md)
                .padding(AxzySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(colors.solid, AxzyShape.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (item.ok) ITIcons.ShieldCheck else ITIcons.Close,
                    contentDescription = null,
                    tint = AxzyColors.surface,
                    modifier = Modifier.size(16.dp),
                )
            }

            ITText(
                text = item.label,
                color = colors.onSoft,
                style = AxzyType.cardBody,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            ITBadge(
                text = if (item.ok) "OK" else "Falla",
                tone = tone,
                dot = false,
            )
        }
    }
}

/**
 * Resumen de una lista de verificación: número de aciertos, porcentaje y una
 * barra de progreso. Se calcula sobre los puntos marcados y no se guarda: si se
 * guardara habría que recalcularlo al marcar, y ese olvido deja el marcador
 * mintiendo.
 */
@Composable
fun ITChecklistScore(
    total: Int,
    ok: Int,
    modifier: Modifier = Modifier,
) {
    if (total <= 0) return
    val percent = ok * 100 / total
    val tone = when {
        percent >= 100 -> Tone.Success
        percent >= 60 -> Tone.Warning
        else -> Tone.Danger
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ITText(
                text = "$ok de $total conformes",
                color = AxzyColors.onSurface,
                style = AxzyType.cardTitle,
            )
            ITBadge(text = "$percent %", tone = tone)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(AxzyColors.surfaceVariant, AxzyShape.pill),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent / 100f)
                    .height(6.dp)
                    .background(tone.palette.solid, AxzyShape.pill),
            )
        }
    }
}
