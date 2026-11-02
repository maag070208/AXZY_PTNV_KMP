package com.axzydev.checkapp.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Canvas
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * En qué punto está una evidencia frente al servidor.
 *
 * Existe porque la subida **puede fallar** y el guardia tiene que poder decidir: si
 * está sin conexión espera, si falló reintenta, y no debería tener que adivinar cuál
 * de las dos cosas pasó.
 */
enum class MediaSyncState {
    /** Todavía local. Sin conexión esto **no** es un error: sube en el próximo sync. */
    LOCAL,

    /** Subiéndose ahora. */
    UPLOADING,

    /** Se intentó y falló: hay algo que reintentar. */
    FAILED,

    /** Ya está en el servidor; el reporte viaja con la URL. */
    UPLOADED,
}

/** Evidencia mostrable: foto o vídeo, con lo necesario para decidir sobre ella. */
data class MediaCard(
    val id: String,
    val label: String,
    val isVideo: Boolean,
    val uri: String,
    val state: MediaSyncState = MediaSyncState.LOCAL,
    val error: String? = null,
)

/**
 * Carrusel de evidencias.
 *
 * Cada evidencia es una **tarjeta de medios**, no una fila con textos debajo: la
 * imagen manda, y lo que hay que saber —cómo se llama y en qué punto está— va encima
 * de ella, sobre un degradado que garantiza la lectura con cualquier foto detrás.
 *
 * Horizontal porque las evidencias se comparan entre sí —cuál salió movida, cuál
 * falta— y en columna empujan el resto del formulario fuera de pantalla.
 *
 * El contenido de cada tarjeta lo pone quien lo usa ([thumbnail]): este componente
 * vive en el sistema de diseño y no sabe leer archivos.
 */
@Composable
fun ITMediaCarousel(
    items: List<MediaCard>,
    onOpen: (MediaCard) -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (MediaCard) -> Unit = {},
    thumbnail: @Composable (MediaCard) -> Unit = { DefaultThumbnail(it) },
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
        contentPadding = PaddingValues(vertical = AxzySpacing.xs),
    ) {
        items(items, key = { it.id }) { item ->
            MediaTile(item = item, onOpen = onOpen, onRetry = onRetry, thumbnail = thumbnail)
        }
    }
}

@Composable
private fun MediaTile(
    item: MediaCard,
    onOpen: (MediaCard) -> Unit,
    onRetry: (MediaCard) -> Unit,
    thumbnail: @Composable (MediaCard) -> Unit,
) {
    Column(
        modifier = Modifier.width(150.dp).clickable { onOpen(item) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AxzySpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(AxzyShape.md)
                .background(AxzyColors.surfaceVariant),
        ) {
            Box(modifier = Modifier.fillMaxSize()) { thumbnail(item) }

            if (item.isVideo) {
                PlayGlyph(modifier = Modifier.align(Alignment.Center))
            }

            StateBadge(
                state = item.state,
                modifier = Modifier.align(Alignment.TopEnd).padding(AxzySpacing.xs),
            )
        }

        // La etiqueta va **fuera** del cuadrado: dentro obligaba a un degradado y a
        // texto encima de la foto, que es justo lo que ensuciaba la tarjeta.
        ITText(
            text = item.label,
            color = AxzyColors.onSurfaceVariant,
            style = AxzyType.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Reintentar sólo cuando hay algo que reintentar, y en pequeño.
        if (item.state == MediaSyncState.FAILED) {
            ITText(
                text = "Reintentar",
                color = Tone.Danger.palette.onSoft,
                style = AxzyType.label,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onRetry(item) }
                    .padding(horizontal = AxzySpacing.sm, vertical = 2.dp),
            )
        }
    }
}

/** Triángulo de reproducción: separa el vídeo de la foto de un vistazo. */
@Composable
private fun PlayGlyph(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, Color.White)
        }
    }
}

/**
 * Estado en un punto, no en un cartel.
 *
 * El estado **no** es el contenido de la tarjeta —eso es la foto—, así que va en un
 * badge de 18 dp con un anillo del color del fondo para contrastar con cualquier
 * imagen. La frase completa vive en el visor.
 */
@Composable
private fun StateBadge(state: MediaSyncState, modifier: Modifier = Modifier) {
    val tone = toneOf(state)

    Box(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(tone.palette.solid),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            MediaSyncState.UPLOADED -> Icon(
                imageVector = ITIcons.Check,
                contentDescription = "En el servidor",
                tint = tone.palette.onSolid,
                modifier = Modifier.size(12.dp),
            )

            MediaSyncState.FAILED -> Icon(
                imageVector = ITIcons.Warning,
                contentDescription = "No se pudo subir",
                tint = tone.palette.onSolid,
                modifier = Modifier.size(12.dp),
            )

            MediaSyncState.UPLOADING -> CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = tone.palette.onSolid,
                strokeWidth = 2.dp,
            )

            MediaSyncState.LOCAL -> Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tone.palette.onSolid),
            )
        }
    }
}

private fun toneOf(state: MediaSyncState): Tone = when (state) {
    MediaSyncState.LOCAL -> Tone.Neutral
    MediaSyncState.UPLOADING -> Tone.Info
    MediaSyncState.FAILED -> Tone.Danger
    MediaSyncState.UPLOADED -> Tone.Success
}

/** Estado en palabras, para el visor: ahí sí cabe la frase entera. */
@Composable
fun MediaStatePill(item: MediaCard) {
    val tone = toneOf(item.state)
    val text = when (item.state) {
        MediaSyncState.LOCAL -> "Sin subir: se enviará con el próximo sync"
        MediaSyncState.UPLOADING -> "Subiendo…"
        MediaSyncState.FAILED -> "No se pudo subir"
        MediaSyncState.UPLOADED -> "En el servidor"
    }

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(tone.palette.soft)
            .padding(horizontal = AxzySpacing.md, vertical = AxzySpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(tone.palette.solid),
        )
        ITText(text = text, color = tone.palette.onSoft, style = AxzyType.itemMeta)
    }
}

@Composable
private fun DefaultThumbnail(item: MediaCard) {
    Icon(
        imageVector = if (item.isVideo) ITIcons.Videocam else ITIcons.PhotoCamera,
        contentDescription = null,
        tint = AxzyColors.onSurfaceVariant,
        modifier = Modifier.size(38.dp),
    )
}

/**
 * Visor de una evidencia, a pantalla completa.
 *
 * Las acciones viven aquí y no en el carrusel: una tarjeta de 160 dp no es sitio para
 * tres botones, y lo que se decide sobre una evidencia —reintentar, quitarla— se
 * decide mirándola.
 */
@Composable
fun ITMediaViewer(
    item: MediaCard,
    onClose: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            // Sin `clickable` propio: el diálogo ya cierra al tocar fuera y con el
            // gesto de atrás. Ponerlo aquí hacía que **cualquier** toque dentro de la
            // tarjeta —mirar la foto— la cerrara.
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f)),
            contentAlignment = Alignment.Center,
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AxzySpacing.lg)
                .clip(AxzyShape.lg)
                .background(AxzyColors.surface)
                .padding(AxzySpacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ITText(text = item.label, color = AxzyColors.onSurface, style = AxzyType.screenTitle)
                Icon(
                    imageVector = ITIcons.Close,
                    contentDescription = "Cerrar",
                    tint = AxzyColors.onSurfaceVariant,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onClose)
                        .padding(AxzySpacing.xs)
                        .size(20.dp),
                )
            }

            Spacer(Modifier.height(AxzySpacing.md))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .clip(AxzyShape.md)
                    .background(AxzyColors.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }

            Spacer(Modifier.height(AxzySpacing.md))
            MediaStatePill(item)

            item.error?.let { message ->
                Spacer(Modifier.height(AxzySpacing.xs))
                ITText(text = message, color = AxzyColors.error, style = AxzyType.itemMeta)
            }

            Spacer(Modifier.height(AxzySpacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
                if (item.state == MediaSyncState.FAILED && onRetry != null) {
                    ITButton(label = "Reintentar", onClick = onRetry, modifier = Modifier.weight(1f))
                }
                ITButton(
                    label = "Quitar",
                    onClick = onRemove,
                    outlined = true,
                    tone = Tone.Danger,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        }
    }
}
