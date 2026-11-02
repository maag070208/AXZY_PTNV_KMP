package com.axzydev.checkapp.pages.checkreport.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITActionTile
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITCardFooter
import com.axzydev.checkapp.design.components.ITChecklist
import com.axzydev.checkapp.design.components.ITChecklistItem
import com.axzydev.checkapp.design.components.ITChecklistScore
import com.axzydev.checkapp.design.components.ITFooterAction
import com.axzydev.checkapp.design.components.ITInlineError
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSectionTitle
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.isDarkMode
import com.axzydev.checkapp.design.theme.palette
import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportAction
import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportUiState
import com.axzydev.checkapp.platform.location.Coordinates
import com.axzydev.checkapp.platform.maps.LocationMap
import kotlin.math.roundToInt

/**
 * Reporte de un punto de control.
 *
 * Se lee de arriba abajo como el trabajo del guardia: **dónde está** (el mapa con la
 * coordenada que se va a guardar), **qué revisa** (la lista de tareas), **qué anota**
 * y **qué adjunta**. La ubicación va en el subtítulo de la barra porque es el contexto
 * de todo lo de abajo.
 *
 * Sin tarjetas contenedoras alrededor de campos y botones: el campo y el tile **ya
 * son** la superficie, y meterlos en una tarjeta es anidar superficie en superficie
 * (§13). La tarjeta se reserva para lo que no tiene superficie propia, como la lista
 * de tareas con su marcador de avance.
 */
@Composable
fun CheckReportScreen(
    state: CheckReportUiState,
    onAction: (CheckReportAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    supportsVideo: Boolean = true,
    onCapturePhoto: () -> Unit = {},
    onCaptureVideo: () -> Unit = {},
    onRefreshLocation: () -> Unit = {},
) {
    val screenState = when {
        state.loading -> ScreenState.Loading
        state.error != null && state.tasks.isEmpty() -> ScreenState.Error(message = state.error)
        else -> ScreenState.Ready
    }

    val photos = state.media.count { !it.isVideo }
    val videos = state.media.count { it.isVideo }

    ITScreenScaffold(
        title = "Punto de control",
        subtitle = state.locationName,
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.xl)) {
            LocationStamp(
                location = state.location,
                locating = state.locating,
                onRefresh = onRefreshLocation,
            )

            if (state.tasks.isNotEmpty()) {
                Section(title = "Tareas del punto") {
                    ITCard(modifier = Modifier.fillMaxWidth()) {
                        ITChecklistScore(total = state.tasks.size, ok = state.tasks.count { it.done })
                        Spacer(Modifier.height(AxzySpacing.md))
                        ITChecklist(
                            items = state.tasks.map { ITChecklistItem(it.id, it.description, it.done) },
                            onToggle = { id, _ -> onAction(CheckReportAction.ToggleTask(id)) },
                        )
                    }
                }
            }

            // El campo ya es una superficie: sin tarjeta alrededor.
            Section(title = "Observaciones") {
                ITTextField(
                    label = "Notas del punto",
                    value = state.notes,
                    onValueChange = { onAction(CheckReportAction.Notes(it)) },
                    singleLine = false,
                    enabled = !state.submitting,
                    placeholder = "Qué encontraste, en dos líneas",
                )
            }

            Section(
                title = "Evidencia",
                action = {
                    if (state.media.isEmpty()) {
                        ITText(
                            text = "Opcional",
                            color = AxzyColors.slate400,
                            style = AxzyType.itemMeta,
                        )
                    } else {
                        ITBadge(
                            text = mediaLabel(photos, videos),
                            tone = Tone.Brand,
                            dot = true,
                        )
                    }
                },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
                    ) {
                        CaptureTile(
                            label = "Tomar foto",
                            icon = ITIcons.PhotoCamera,
                            enabled = !state.submitting,
                            onClick = onCapturePhoto,
                            modifier = Modifier.weight(1f),
                        )
                        if (supportsVideo) {
                            CaptureTile(
                                label = "Grabar vídeo",
                                icon = ITIcons.Videocam,
                                enabled = !state.submitting,
                                onClick = onCaptureVideo,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    state.media.forEachIndexed { index, media ->
                        ITListItem(
                            title = if (media.isVideo) "Vídeo ${index + 1}" else "Foto ${index + 1}",
                            subtitle = "Se enviará con la marcación",
                            avatarInitial = (index + 1).toString(),
                            avatarStatus = Tone.Brand,
                            footer = {
                                ITCardFooter {
                                    ITFooterAction(
                                        label = "Quitar",
                                        onClick = { onAction(CheckReportAction.RemoveMedia(media.id)) },
                                        tone = Tone.Danger,
                                    )
                                }
                            },
                        )
                    }

                    if (state.media.isEmpty()) {
                        ITText(
                            text = "Una foto del punto resuelve la mitad de las dudas.",
                            color = AxzyColors.onSurfaceVariant,
                            style = AxzyType.itemMeta,
                        )
                    }
                }
            }

            // El fallo del formulario se corrige aquí mismo; no justifica sacar al
            // usuario de la pantalla como sí hace el error de carga.
            state.error?.let { message -> ITInlineError(message) }

            ITButton(
                label = "Enviar reporte",
                onClick = { onAction(CheckReportAction.Submit) },
                loading = state.submitting,
                enabled = !state.submitting,
            )
        }
    }
}

/** Bloque con su encabezado, con la misma forma que en el reporte de incidencias. */
@Composable
private fun Section(
    title: String,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
        ITSectionTitle(text = title, action = action)
        content()
    }
}

/** Acción de captura: el mismo tile de las acciones rápidas del inicio. */
@Composable
private fun CaptureTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ITActionTile(
        label = label,
        onClick = onClick,
        tone = Tone.Brand,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Tone.Brand.palette.solid,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}

/**
 * Dónde queda la marcación.
 *
 * El mapa es la prueba visual de que el guardia estaba en el punto, y debajo va la
 * **coordenada exacta que se guarda**: el mapa se lee de un vistazo y el número se
 * puede citar. La precisión va **encima del mapa**, que es de quien es el dato; sin
 * posición no se pinta un mapa vacío —que parece una app colgada—: se dice, y la
 * marcación sigue sin coordenadas.
 */
@Composable
private fun LocationStamp(
    location: Coordinates?,
    locating: Boolean,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MAP_HEIGHT)
                .clip(AxzyShape.md),
        ) {
            when {
                location != null -> LocationMap(
                    location = location,
                    modifier = Modifier.fillMaxSize(),
                    dark = isDarkMode,
                )

                locating -> MapNotice(text = "Buscando ubicación…")

                else -> MapNotice(text = "Sin ubicación")
            }

            location?.accuracyMeters?.let { accuracy ->
                ITText(
                    text = "±${accuracy.roundToInt()} m",
                    color = AxzyColors.onSurface,
                    style = AxzyType.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(AxzySpacing.sm)
                        .background(AxzyColors.surface, AxzyShape.pill)
                        .border(1.dp, AxzyColors.outlineVariant, AxzyShape.pill)
                        .padding(horizontal = AxzySpacing.sm, vertical = 2.dp),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
        ) {
            Icon(
                imageVector = ITIcons.Place,
                contentDescription = null,
                tint = if (location != null) Tone.Brand.palette.solid else AxzyColors.slate400,
                modifier = Modifier.size(16.dp),
            )
            ITText(
                text = location?.summary() ?: "Se guardará sin coordenadas",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.itemMeta,
                modifier = Modifier.weight(1f),
            )
            if (!locating) {
                ITTouchableOpacity(onClick = onRefresh, scaleTo = 0.94f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.xs),
                    ) {
                        Icon(
                            imageVector = ITIcons.Repeat,
                            contentDescription = null,
                            tint = Tone.Brand.palette.solid,
                            modifier = Modifier.size(14.dp),
                        )
                        ITText(
                            text = "Actualizar",
                            color = Tone.Brand.palette.solid,
                            style = AxzyType.itemMeta,
                        )
                    }
                }
            }
        }
    }
}

/** Hueco del mapa cuando todavía no hay posición. */
@Composable
private fun MapNotice(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AxzyColors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        ITText(text = text, color = AxzyColors.slate400, style = AxzyType.itemMeta)
    }
}

/**
 * Coordenada legible: cinco decimales son ~1 m, más de lo que da cualquier GPS de
 * teléfono. `String.format` no existe en común, así que el redondeo es a mano.
 */
private fun Coordinates.summary(): String = "${latitude.round5()}, ${longitude.round5()}"

private fun Double.round5(): String = (kotlin.math.round(this * 100_000) / 100_000.0).toString()

/** "2 fotos · 1 vídeo" — sólo lo que hay, para no anunciar ceros. */
private fun mediaLabel(photos: Int, videos: Int): String {
    val parts = buildList {
        if (photos > 0) add(if (photos == 1) "1 foto" else "$photos fotos")
        if (videos > 0) add(if (videos == 1) "1 vídeo" else "$videos vídeos")
    }
    return parts.joinToString(" · ")
}

private val MAP_HEIGHT = 168.dp
