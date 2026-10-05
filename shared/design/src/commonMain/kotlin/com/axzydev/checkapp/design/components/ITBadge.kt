package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * Pastilla de estado. Se usa para estados, roles y filtros.
 *
 * El tono es un `enum`, no un color suelto: así no se puede inventar un badge
 * con un color que no esté en la paleta, que es el fallo habitual cuando el
 * diseño se hace a mano pantalla por pantalla.
 *
 * Con `dot = true` añade un punto de color a la izquierda, útil para estados
 * "en vivo".
 */
@Composable
fun ITBadge(
    text: String,
    tone: Tone = Tone.Neutral,
    modifier: Modifier = Modifier,
    dot: Boolean = false,
) {
    val colors = tone.palette

    Row(
        modifier = modifier
            .background(colors.soft, AxzyShape.pill)
            .border(1.dp, colors.border, AxzyShape.pill)
            .padding(horizontal = AxzySpacing.sm, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.xs),
    ) {
        if (dot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(colors.solid, AxzyShape.pill),
            )
        }
        ITText(
            text = text.uppercase(),
            color = colors.onSoft,
            style = AxzyType.labelSmall,
        )
    }
}

/**
 * Mapa de tono por estado textual.
 *
 * Centraliza la traducción "estado de negocio → color" para que dos pantallas
 * no pinten el mismo estado de distinto color.
 */
fun toneForStatus(status: String?): Tone = when (status?.uppercase()) {
    "ACTIVE", "ACTIVO", "COMPLETED", "COMPLETADA", "RESOLVED", "RESUELTO",
    "ATTENDED", "ATENDIDA", "ATENDIDO", "DONE", "SYNCED", "OK" -> Tone.Success
    "PENDING", "PENDIENTE", "IN_PROGRESS", "EN CURSO", "WARNING", "CHECKING", "UNDER_REVIEW" -> Tone.Warning
    "ANOMALY", "ERROR", "FAILED", "CANCELLED", "CANCELADA", "DISMISSED", "OVERDUE", "MISSED" -> Tone.Danger
    "INFO", "REVIEWED", "REVISADA" -> Tone.Info
    "DISCIPLINE", "DISCIPLINA" -> Tone.Accent
    else -> Tone.Neutral
}

/**
 * Mapa de rol → tono.
 *
 * Se centraliza para que el mismo rol no salga verde en el listado de usuarios y
 * gris en el menú. Los nombres son los que devuelve el backend; si aparece uno
 * nuevo sin mapear cae en `Neutral`, que es visible pero no alarmante.
 *
 * No se usa el color para jerarquizar (un admin no es "mejor" que un guardia):
 * el color aquí sólo ayuda a distinguir filas de un vistazo.
 */
fun toneForRole(role: String?): Tone = when (role?.uppercase()) {
    "ADMIN", "ADMINISTRADOR" -> Tone.Brand
    "LIDER", "SHIFT", "SUPERVISOR" -> Tone.Info
    "GUARD", "GUARDIA" -> Tone.Success
    "MAINT", "MANTENIMIENTO" -> Tone.Warning
    "RESDN", "USUARIO" -> Tone.Accent
    else -> Tone.Neutral
}
