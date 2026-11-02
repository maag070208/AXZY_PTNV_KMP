package com.axzydev.checkapp.pages.reportissue.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITInlineError
import com.axzydev.checkapp.design.components.ITMediaCarousel
import com.axzydev.checkapp.design.components.ITMediaViewer
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSectionTitle
import com.axzydev.checkapp.design.components.ITSkeletonList
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.components.MediaCard
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.parseHexColor
import com.axzydev.checkapp.design.theme.palette
import com.axzydev.checkapp.design.theme.toneForColor
import com.axzydev.checkapp.pages.reportissue.viewmodel.MIN_DESCRIPTION_LENGTH
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueAction
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueKind
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueUiState
import com.axzydev.checkapp.platform.media.LocalImage
import com.axzydev.checkapp.platform.media.LocalVideoFrame
import com.axzydev.checkapp.platform.media.rememberMediaOpener

/**
 * Reportar una incidencia o una falla de mantenimiento.
 *
 * Pensada para llenarse rápido, con una mano y sin hacer scroll:
 * - **Secciones simples**, sin numeración ni línea de tiempo: el orden ya lo da la
 *   pantalla de arriba hacia abajo.
 * - **Píldoras** para categoría y tipo: compactas, con el color de la categoría al
 *   elegirla. El tipo aparece (animado) sólo si la categoría tiene sub-tipos.
 * - **Sin campo de título**: el reporte se titula solo con la categoría y el tipo,
 *   así que el guardia sólo escribe qué pasó.
 * - **Evidencia en el encabezado**: cámara y vídeo son iconos junto al encabezado de
 *   la sección; el carrusel sólo ocupa espacio cuando hay algo que mostrar.
 * - **Pie de un solo elemento**: el botón dice qué falta mientras no se pueda enviar,
 *   así no hace falta una tarjeta de requisitos.
 */
@Composable
fun ReportIssueScreen(
    state: ReportIssueUiState,
    onAction: (ReportIssueAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    supportsVideo: Boolean = true,
    onCapturePhoto: () -> Unit = {},
    onCaptureVideo: () -> Unit = {},
) {
    val screenState = when {
        state.loading && state.categories.isEmpty() -> ScreenState.Loading

        state.loadError != null ->
            ScreenState.Error(state.loadError, onRetry = { onAction(ReportIssueAction.Retry) })

        // Sin catálogo no hay nada que reportar: se dice y se ofrece reintentar.
        state.categories.isEmpty() -> ScreenState.Empty(
            title = "Sin categorías",
            description = "Sincroniza para recibir el catálogo y poder reportar.",
            actionLabel = "Reintentar",
            onAction = { onAction(ReportIssueAction.Retry) },
        )

        else -> ScreenState.Ready
    }

    // Qué evidencia está abierta. Va en el view-model y no en un `remember` local:
    // con el estado local el toque en la tarjeta no llegaba a abrir el visor.
    val openMedia = state.media.firstOrNull { it.id == state.openMediaId }
    val openWithSystem = rememberMediaOpener()

    // Convierte la evidencia en la `MediaCard` que pintan el carrusel y el visor.
    // Función local para inferir el tipo desde `state.media` sin depender de su nombre.
    fun cardFor(index: Int): MediaCard {
        val media = state.media[index]
        return MediaCard(
            id = media.id,
            label = if (media.isVideo) "Vídeo ${index + 1}" else "Foto ${index + 1}",
            isVideo = media.isVideo,
            uri = media.uri,
            state = media.state,
            error = media.error,
        )
    }

    val selectedCategory = state.selectedCategory
    val types = selectedCategory?.types.orEmpty()
    val categoryTone = toneForColor(parseHexColor(selectedCategory?.color))
    val isIncident = state.kind == ReportIssueKind.INCIDENT

    // El botón explica por qué está apagado: un solo elemento en vez de botón + tarjeta.
    val missing = state.missingRequirements
    val submitLabel = when {
        state.canSubmit || missing.isEmpty() -> "Enviar reporte"
        missing.size == 1 -> "Falta: ${missing.first()}"
        else -> "Falta: ${missing.first()} (+${missing.size - 1} más)"
    }

    Box(modifier = modifier.fillMaxSize()) {
        ITScreenScaffold(
            title = state.kind.label,
            state = screenState,
            onBack = onBack,
            skeleton = { ITSkeletonList(rows = 5) },
            actions = {
                ITBadge(
                    text = if (isIncident) "Incidencia" else "Falla",
                    tone = if (isIncident) Tone.Danger else Tone.Warning,
                    dot = true,
                )
            },
            modifier = Modifier.fillMaxSize(),
            // El scroll lo maneja la pantalla: el botón va fijo abajo.
            scroll = false,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(AxzySpacing.xl),
                ) {
                    // ── Categoría (+ tipo) ──────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
                        ITSectionTitle(text = "Categoría")

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                        ) {
                            state.categories.forEach { option ->
                                val selected = option.id == state.selectedCategoryId
                                ChoicePill(
                                    label = option.name,
                                    selected = selected,
                                    // Sin elegir va en neutro: el color aparece al elegir.
                                    tone = if (selected) {
                                        toneForColor(parseHexColor(option.color))
                                    } else {
                                        Tone.Neutral
                                    },
                                    icon = categoryIcon(option.icon),
                                    onClick = { onAction(ReportIssueAction.SelectCategory(option.id)) },
                                )
                            }
                        }

                        // El tipo sólo existe si la categoría tiene sub-tipos.
                        AnimatedVisibility(visible = types.isNotEmpty()) {
                            Column(
                                modifier = Modifier.padding(top = AxzySpacing.xs),
                                verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                            ) {
                                ITText(
                                    text = if (isIncident) "Tipo de incidencia" else "Tipo de falla",
                                    color = AxzyColors.onSurfaceVariant,
                                    style = AxzyType.itemMeta,
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                                    verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                                ) {
                                    types.forEach { option ->
                                        ChoicePill(
                                            label = option.name,
                                            selected = option.id == state.selectedTypeId,
                                            tone = categoryTone,
                                            icon = null,
                                            onClick = { onAction(ReportIssueAction.SelectType(option.id)) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Detalles ────────────────────────────────────────────────
                    // Sin campo de título: el reporte se titula solo con la categoría
                    // y el tipo elegidos, así que aquí sólo queda explicar qué pasó.
                    Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
                        ITSectionTitle(text = "Descripción")
                        Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.fieldGap)) {
                            ITTextField(
                                label = "Descripción *",
                                value = state.description,
                                onValueChange = { onAction(ReportIssueAction.Description(it)) },
                                singleLine = false,
                                placeholder = "Qué pasó, dónde y desde cuándo",
                                // Se avisa sólo cuando ya escribió algo: marcar error en un
                                // campo vacío que todavía no tocó es regañar antes de tiempo.
                                error = if (state.description.isNotEmpty() && !state.descriptionValid) {
                                    "Mínimo $MIN_DESCRIPTION_LENGTH caracteres (faltan ${state.descriptionMissing})"
                                } else {
                                    null
                                },
                                enabled = !state.saving,
                            )
                        }
                    }

                    // ── Evidencia (opcional) ────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
                        ITSectionTitle(
                            text = "Evidencia (opcional)",
                            action = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                                ) {
                                    if (state.media.isNotEmpty()) {
                                        ITText(
                                            text = mediaLabel(
                                                photos = state.media.count { !it.isVideo },
                                                videos = state.media.count { it.isVideo },
                                            ),
                                            color = AxzyColors.onSurfaceVariant,
                                            style = AxzyType.itemMeta,
                                        )
                                    }
                                    RoundIconButton(icon = ITIcons.PhotoCamera, onClick = onCapturePhoto)
                                    if (supportsVideo) {
                                        RoundIconButton(icon = ITIcons.Videocam, onClick = onCaptureVideo)
                                    }
                                }
                            },
                        )

                        if (state.media.isEmpty()) {
                            ITText(
                                text = "Una foto ayuda a ubicar el problema.",
                                color = AxzyColors.onSurfaceVariant,
                                style = AxzyType.itemMeta,
                            )
                        } else {
                            ITMediaCarousel(
                                items = state.media.indices.map { cardFor(it) },
                                onOpen = { onAction(ReportIssueAction.OpenMedia(it.id)) },
                                onRetry = { onAction(ReportIssueAction.RetryMedia(it.id)) },
                                thumbnail = { item ->
                                    // El vídeo se representa con su primer fotograma, no
                                    // con un icono: una caja gris no dice qué se grabó.
                                    if (item.isVideo) {
                                        LocalVideoFrame(uri = item.uri, modifier = Modifier.fillMaxSize())
                                    } else {
                                        LocalImage(uri = item.uri, modifier = Modifier.fillMaxSize())
                                    }
                                },
                            )
                        }
                    }

                    Spacer(Modifier.height(AxzySpacing.sm))
                }

                // Pie fijo: error (si hay) y el botón, que además dice qué falta.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AxzyColors.background)
                        .padding(top = AxzySpacing.sm)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                ) {
                    state.error?.let { message -> ITInlineError(message) }

                    ITButton(
                        label = submitLabel,
                        onClick = { onAction(ReportIssueAction.Submit) },
                        loading = state.saving,
                        enabled = state.canSubmit,
                    )
                }
            }
        }

        // Visor de evidencia a pantalla completa.
        openMedia?.let { media ->
            val item = cardFor(state.media.indexOf(media))
            ITMediaViewer(
                item = item,
                onClose = { onAction(ReportIssueAction.CloseMedia) },
                onRetry = { onAction(ReportIssueAction.RetryMedia(item.id)) },
                onRemove = { onAction(ReportIssueAction.RemoveMedia(item.id)) },
            ) {
                if (item.isVideo) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = ITIcons.Videocam,
                            contentDescription = null,
                            tint = AxzyColors.onSurfaceVariant,
                            modifier = Modifier.size(52.dp),
                        )
                        Spacer(Modifier.height(AxzySpacing.sm))
                        ITButton(
                            label = "Reproducir",
                            onClick = { openWithSystem(item.uri) },
                            outlined = true,
                            compact = true,
                        )
                    }
                } else {
                    LocalImage(uri = item.uri, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/**
 * Píldora de elección única, para categoría y tipo.
 *
 * Compacta (40 dp de alto mínimo) y envolvente: frases de largo muy distinto
 * —"Robo / Hurto" y "Arma de Fuego / Blanca"— conviven sin columnas fijas.
 * Elegida se tiñe con el tono; sin elegir queda neutra.
 */
@Composable
private fun ChoicePill(
    label: String,
    selected: Boolean,
    tone: Tone,
    icon: ImageVector?,
    onClick: () -> Unit,
) {
    val colors = tone.palette

    ITTouchableOpacity(onClick = onClick, scaleTo = 0.96f) {
        Row(
            modifier = Modifier
                .heightIn(min = 40.dp)
                .background(
                    color = if (selected) colors.soft else AxzyColors.surface,
                    shape = AxzyShape.pill,
                )
                .border(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) colors.solid else AxzyColors.outlineVariant,
                    shape = AxzyShape.pill,
                )
                .padding(horizontal = AxzySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.xs + 2.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) colors.solid else AxzyColors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            } else if (selected) {
                Icon(
                    imageVector = ITIcons.Check,
                    contentDescription = null,
                    tint = colors.solid,
                    modifier = Modifier.size(14.dp),
                )
            }
            ITText(
                text = label,
                color = if (selected) colors.onSoft else AxzyColors.onSurface,
                style = AxzyType.itemMeta,
            )
        }
    }
}

/** Botón redondo de 36 dp con un icono: cámara y vídeo en el encabezado de evidencia. */
@Composable
private fun RoundIconButton(icon: ImageVector, onClick: () -> Unit) {
    ITTouchableOpacity(onClick = onClick, scaleTo = 0.92f) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(AxzyColors.surface, AxzyShape.pill)
                .border(1.dp, AxzyColors.outlineVariant, AxzyShape.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AxzyColors.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Icono de la categoría a partir del nombre que trae el catálogo.
 *
 * El API guarda nombres de Material Design Icons ("shield-alert", "water-pump"). Se
 * traducen a los iconos del sistema y **lo desconocido cae en un icono genérico**: una
 * categoría nueva creada desde el panel no debe salir con un hueco.
 */
private fun categoryIcon(name: String?): ImageVector = when (name) {
    "shield-alert", "shield", "shield-check" -> ITIcons.ShieldCheck
    "account-group", "account-multiple", "account" -> ITIcons.Person
    "water-pump", "water_drop", "water", "pipe" -> ITIcons.WaterDrop
    "lightning-bolt", "bolt", "flash" -> ITIcons.Bolt
    "home-city", "office-building", "domain", "building" -> ITIcons.Business
    "pine-tree", "park", "tree" -> ITIcons.Park
    "toolbox", "wrench", "tools" -> ITIcons.Wrench
    else -> ITIcons.Layers
}

/** "2 fotos · 1 vídeo" — sólo lo que hay, para no anunciar ceros. */
private fun mediaLabel(photos: Int, videos: Int): String {
    val parts = buildList {
        if (photos > 0) add(if (photos == 1) "1 foto" else "$photos fotos")
        if (videos > 0) add(if (videos == 1) "1 vídeo" else "$videos vídeos")
    }
    return parts.joinToString(" · ")
}