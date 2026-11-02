package com.axzydev.checkapp.pages.guarddashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import com.axzydev.checkapp.platform.camera.QrScannerView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.core.common.time.currentGreeting
import com.axzydev.checkapp.design.components.ITActionTile
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITBrandHero
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITConfirmDialog
import com.axzydev.checkapp.design.components.ITEmptyState
import com.axzydev.checkapp.design.components.ITFeatureBadge
import com.axzydev.checkapp.design.components.ITFeatureCard
import com.axzydev.checkapp.design.components.ITHeroIconButton
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITSectionTitle
import com.axzydev.checkapp.design.components.ITSkeletonList
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.components.MenuBars
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyAlpha
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.GuardDashboardAction
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.GuardDashboardUiState
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.PointUi
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.RouteUi

/**
 * Panel del guardia.
 *
 * La pantalla se lee de arriba abajo como el turno: **quién eres** (cabecera de
 * marca), **dónde vas** (el bloque del escáner, o las rutas por iniciar), **qué
 * reportas** (las dos acciones rápidas) y **qué queda** (los puntos de la ruta).
 *
 * El bloque oscuro es negro y no verde a propósito: el verde se reserva para los
 * botones que inician la acción, así que el sitio de la acción principal no compite
 * con ella.
 *
 * Iniciar una ruta **siempre pasa por una decisión explícita**: con una sola ruta
 * el bloque la inicia; con varias pregunta cuál; y tocar una ruta de la lista pide
 * confirmación. Antes se iniciaba la primera sin preguntar, que con dos rutas
 * asignadas es elegir por el guardia.
 *
 * Los accesos (historial, notificaciones, perfil y salir) se quitaron de aquí: son
 * los mismos que ya están en el menú lateral, y en el panel del guardia empujaban
 * los puntos de la ruta —lo único que se consulta a media ronda— fuera de pantalla.
 */
@Composable
fun GuardDashboardScreen(
    state: GuardDashboardUiState,
    onAction: (GuardDashboardAction) -> Unit,
    onOpenCheck: (roundId: String, locationId: String) -> Unit,
    onScan: () -> Unit,
    onQrCode: (String) -> Unit,
    onReportIncident: () -> Unit,
    onReportMaintenance: () -> Unit,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenMenu: () -> Unit = {},
) {
    // Primer arranque: todavía no hay ni una ruta ni una ronda en curso. Es el
    // único caso en el que un esqueleto es lo correcto; si ya hay datos, un
    // refresco no debe borrar la pantalla.
    val firstLoad = state.loading && state.routes.isEmpty() && !state.hasActiveRound

    Surface(modifier = modifier.fillMaxSize(), color = AxzyColors.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            GuardHero(
                userName = state.userName,
                onOpenMenu = onOpenMenu,
                onSync = onSync,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                        ),
                    )
                    .padding(horizontal = AxzySpacing.xl),
            ) {
                Spacer(Modifier.height(AxzySpacing.lg))

                when {
                    firstLoad -> ITSkeletonList(rows = 4)

                    state.error != null && !state.hasActiveRound && state.routes.isEmpty() ->
                        ITEmptyState(
                            title = "No se pudo cargar tu panel",
                            description = state.error,
                            actionLabel = "Reintentar",
                            onAction = { onAction(GuardDashboardAction.Refresh) },
                            icon = {
                                Icon(
                                    imageVector = ITIcons.ErrorOutline,
                                    contentDescription = null,
                                    tint = AxzyColors.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp),
                                )
                            },
                        )

                    else -> {
                        if (state.hasActiveRound) {
                            ScanCard(
                                points = state.points,
                                scanning = state.scanning,
                                onScan = onScan,
                                onQrCode = onQrCode,
                                onCloseScan = { onAction(GuardDashboardAction.CloseScanner) },
                                onEnd = { onAction(GuardDashboardAction.EndRound) },
                            )
                        } else {
                            StartCard(
                                routes = state.routes,
                                onRequestStart = { onAction(GuardDashboardAction.RequestStart) },
                                onPickRoute = {
                                    onAction(GuardDashboardAction.RequestStartRoute(it))
                                },
                                onSync = onSync,
                            )
                        }

                        Spacer(Modifier.height(AxzySpacing.md))

                        QuickReports(
                            onReportIncident = onReportIncident,
                            onReportMaintenance = onReportMaintenance,
                        )

                        if (state.hasActiveRound && state.points.isNotEmpty()) {
                            Spacer(Modifier.height(AxzySpacing.xxl))
                            ITSectionTitle(
                                text = "Ruta en curso",
                                action = {
                                    ITBadge(
                                        text = "${state.points.count { it.verified }} de " +
                                            "${state.points.size}",
                                        tone = Tone.Brand,
                                        dot = true,
                                    )
                                },
                            )
                            Spacer(Modifier.height(AxzySpacing.sm))

                            val nextId = state.points.firstOrNull { !it.verified }?.locationId
                            state.points.forEach { point ->
                                PointRow(
                                    point = point,
                                    isNext = point.locationId == nextId,
                                    onClick = {
                                        state.activeRoundId?.let { id ->
                                            onOpenCheck(id, point.locationId)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(AxzySpacing.xxxl))
            }
        }
    }

    // Confirmación de una ruta concreta (se tocó su fila).
    state.pendingStart?.let { route ->
        ITConfirmDialog(
            title = "Iniciar ruta",
            message = "¿Iniciar «${route.title}»? Tendrás que escanear " +
                "${pointsLabel(route.pointCount)}.",
            confirmLabel = "Iniciar",
            // No es una acción destructiva: el botón va en verde de marca, no en rojo.
            tone = Tone.Brand,
            loading = state.starting,
            onConfirm = { onAction(GuardDashboardAction.ConfirmStart) },
            onDismiss = { onAction(GuardDashboardAction.CancelStart) },
        )
    }

    if (state.routePickerOpen) {
        RoutePickerDialog(
            routes = state.routes,
            onPick = { onAction(GuardDashboardAction.PickRoute(it)) },
            onDismiss = { onAction(GuardDashboardAction.DismissRoutePicker) },
        )
    }

    state.panic?.let { panic ->
        PanicOverlay(
            title = panic.title,
            message = panic.message,
            onDismiss = { onAction(GuardDashboardAction.DismissPanic) },
        )
    }
}

/**
 * Cabecera de marca: saludo y nombre sobre el degradado, con menú y sincronizar.
 *
 * Misma pieza que el inicio del administrador para que las dos apps —la del guardia
 * y la del supervisor— se reconozcan como la misma. El saludo es dinámico
 * ("Buenos días / Buenas tardes / Buenas noches") y sale del helper compartido.
 */
@Composable
private fun GuardHero(
    userName: String,
    onOpenMenu: () -> Unit,
    onSync: () -> Unit,
) {
    ITBrandHero(
        title = userName.ifBlank { "Guardia" },
        subtitle = currentGreeting(),
        compact = true,
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
                ITHeroIconButton(
                    contentDescription = "Sincronizar",
                    onClick = onSync,
                ) {
                    Icon(
                        imageVector = ITIcons.Repeat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                ITHeroIconButton(
                    contentDescription = "Abrir menú",
                    onClick = onOpenMenu,
                ) {
                    MenuBars(tint = Color.White)
                }
            }
        },
    )
}

/**
 * Escáner activo.
 *
 * El avance va **dentro** del bloque y en número ("3 de 9"), porque es la pregunta
 * que se hace un guardia a media ruta, y debajo el nombre del punto que toca. El
 * botón de escanear es el único elemento verde de la pantalla: es la acción que se
 * repite cien veces por turno.
 */
@Composable
private fun ScanCard(
    points: List<PointUi>,
    scanning: Boolean,
    onScan: () -> Unit,
    onQrCode: (String) -> Unit,
    onCloseScan: () -> Unit,
    onEnd: () -> Unit,
) {
    // El escáner ocupa **la misma tarjeta**, no una capa encima ni otra pantalla:
    // es el mismo recuadro en el que estaba el botón que se acaba de tocar.
    if (scanning) {
        ITFeatureCard {
            Column(
                modifier = Modifier.padding(AxzySpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AxzySpacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(AxzyShape.lg),
                ) {
                    QrScannerView(onQrCode = onQrCode, modifier = Modifier.fillMaxSize())
                }

                ITButton(
                    label = "Cancelar",
                    onClick = onCloseScan,
                    outlined = true,
                    compact = true,
                )
            }
        }
        return
    }

    val verified = points.count { it.verified }
    val total = points.size
    val next = points.firstOrNull { !it.verified }

    ITFeatureCard {
        Column(
            modifier = Modifier.padding(horizontal = AxzySpacing.xl, vertical = AxzySpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AxzySpacing.md),
        ) {
            ITFeatureBadge(size = 64.dp) {
                Icon(
                    imageVector = ITIcons.Search,
                    contentDescription = null,
                    tint = AxzyColors.primary,
                    modifier = Modifier.size(30.dp),
                )
            }

            if (total > 0) {
                ITText(
                    text = "$verified de $total puntos",
                    color = Color.White,
                    style = AxzyType.screenTitle,
                    textAlign = TextAlign.Center,
                )

                LinearProgressIndicator(
                    progress = { verified.toFloat() / total.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = AxzyColors.primary,
                    trackColor = Color.White.copy(alpha = AxzyAlpha.onBrandSurface),
                )

                ITText(
                    text = next?.let { "Siguiente: ${it.name}" } ?: "Ruta completa",
                    color = Color.White.copy(alpha = AxzyAlpha.onBrandMuted),
                    style = AxzyType.itemMeta,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                ITText(
                    text = "Ruta en curso",
                    color = Color.White,
                    style = AxzyType.cardTitle,
                    textAlign = TextAlign.Center,
                )
                ITText(
                    text = "Sincroniza para ver los puntos",
                    color = Color.White.copy(alpha = AxzyAlpha.onBrandMuted),
                    style = AxzyType.itemMeta,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(AxzySpacing.xs))

            ITButton(
                label = "Escanear",
                onClick = onScan,
                modifier = Modifier.fillMaxWidth(),
            )

            ITButton(
                label = "Finalizar ruta",
                onClick = onEnd,
                outlined = true,
                tone = Tone.Danger,
                compact = true,
            )
        }
    }
}

/**
 * Sin ruta activa: las rutas asignadas, listas para empezar.
 *
 * El bloque entero es pulsable —y lleva su botón— porque es la acción del turno;
 * sin rutas se convierte en el sitio desde el que se sincroniza, que es lo único
 * que se puede hacer entonces.
 */
@Composable
private fun StartCard(
    routes: List<RouteUi>,
    onRequestStart: () -> Unit,
    onPickRoute: (String) -> Unit,
    onSync: () -> Unit,
) {
    val hasRoutes = routes.isNotEmpty()

    Column {
        ITTouchableOpacity(
            onClick = { if (hasRoutes) onRequestStart() else onSync() },
            scaleTo = 0.98f,
        ) {
            // Sin botón, el bloque no necesita el alto que reservaba para él.
            ITFeatureCard(height = 150.dp) {
                Column(
                    modifier = Modifier.padding(horizontal = AxzySpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                ) {
                    ITFeatureBadge(size = if (hasRoutes) 64.dp else 56.dp) {
                        Icon(
                            imageVector = if (hasRoutes) ITIcons.Walk else ITIcons.Route,
                            contentDescription = null,
                            tint = AxzyColors.primary,
                            modifier = Modifier.size(if (hasRoutes) 30.dp else 26.dp),
                        )
                    }

                    ITText(
                        text = if (hasRoutes) {
                            if (routes.size == 1) "Inicia tu ruta" else "Elige tu ruta"
                        } else {
                            "Sin rutas asignadas"
                        },
                        color = Color.White,
                        style = AxzyType.cardTitle,
                        textAlign = TextAlign.Center,
                    )

                    // Sin botón: el bloque entero es el objetivo y el texto dice qué
                    // pasa al tocarlo. Un botón dentro de una tarjeta pulsable son
                    // dos zonas para la misma acción y la mitad de la gente pulsa la
                    // que no hace nada.
                    ITText(
                        text = when {
                            !hasRoutes -> "Presiona aquí para sincronizar"
                            routes.size == 1 -> "Presiona aquí para iniciar ruta"
                            else -> "Presiona aquí para elegir tu ruta"
                        },
                        color = Color.White.copy(alpha = AxzyAlpha.onBrandMuted),
                        style = AxzyType.itemMeta,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        if (hasRoutes) {
            Spacer(Modifier.height(AxzySpacing.md))
            routes.forEach { route ->
                ITListItem(
                    title = route.title,
                    subtitle = pointsLabel(route.pointCount),
                    leadingIcon = { RowIcon(ITIcons.Route, Tone.Brand) },
                    badge = { Chevron() },
                    onClick = { onPickRoute(route.id) },
                )
            }
        }
    }
}

/**
 * Selector de ruta, cuando hay más de una asignada.
 *
 * Se abre sólo desde el bloque del escáner: tocar una ruta de la lista ya es elegir,
 * así que esa va directa a su confirmación.
 */
@Composable
private fun RoutePickerDialog(
    routes: List<RouteUi>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AxzyColors.surface,
        shape = AxzyShape.lg,
        title = {
            ITText(
                text = "¿Qué ruta inicias?",
                color = AxzyColors.onSurface,
                style = AxzyType.cardTitle,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
                routes.forEach { route ->
                    ITListItem(
                        title = route.title,
                        subtitle = pointsLabel(route.pointCount),
                        leadingIcon = { RowIcon(ITIcons.Route, Tone.Brand) },
                        onClick = { onPick(route.id) },
                    )
                }
            }
        },
        // El `AlertDialog` exige un botón de confirmación; aquí la confirmación es
        // elegir una ruta, así que sólo se ofrece salir.
        confirmButton = {},
        dismissButton = {
            ITButton(
                label = "Cancelar",
                onClick = onDismiss,
                outlined = true,
                compact = true,
                tone = Tone.Neutral,
            )
        },
    )
}

/** Incidencia y mantenimiento: las dos cosas que se reportan desde la calle. */
@Composable
private fun QuickReports(
    onReportIncident: () -> Unit,
    onReportMaintenance: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
    ) {
        ITActionTile(
            label = "Incidencia",
            onClick = onReportIncident,
            tone = Tone.Danger,
            modifier = Modifier.weight(1f),
            icon = { RowIcon(ITIcons.Warning, Tone.Danger, size = 18.dp) },
        )
        ITActionTile(
            label = "Mantenimiento",
            onClick = onReportMaintenance,
            tone = Tone.Warning,
            modifier = Modifier.weight(1f),
            icon = { RowIcon(ITIcons.Wrench, Tone.Warning, size = 18.dp) },
        )
    }
}

/** Icono de fila, sin recuadro. */
@Composable
private fun RowIcon(icon: ImageVector, tone: Tone, size: androidx.compose.ui.unit.Dp = 20.dp) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (tone == Tone.Neutral) AxzyColors.slate400 else tone.palette.solid,
        modifier = Modifier.size(size),
    )
}

/** Flecha de "esto lleva a otra pantalla". */
@Composable
private fun Chevron() {
    Icon(
        imageVector = ITIcons.ArrowForward,
        contentDescription = null,
        tint = AxzyColors.slate400,
        modifier = Modifier.size(18.dp),
    )
}

/**
 * Punto de la ruta activa.
 *
 * El siguiente sin verificar va marcado: en una ruta de nueve puntos, una lista de
 * nueve filas iguales no dice por dónde va el guardia. Los ya verificados se
 * atenúan para que la vista caiga en lo que falta.
 */
@Composable
private fun PointRow(point: PointUi, isNext: Boolean, onClick: () -> Unit) {
    ITListItem(
        title = point.name,
        subtitle = when {
            point.verified -> "Verificado"
            isNext -> "Siguiente · ${tasksLabel(point.taskCount)}"
            else -> tasksLabel(point.taskCount)
        },
        avatarInitial = point.name,
        avatarStatus = if (point.verified) Tone.Success else Tone.Warning,
        badge = when {
            point.verified -> {
                { ITBadge(text = "Hecho", tone = Tone.Success, dot = true) }
            }
            isNext -> {
                { ITBadge(text = "Ahora", tone = Tone.Brand, dot = true) }
            }
            else -> null
        },
        onClick = onClick,
    )
}

private fun tasksLabel(count: Int): String = if (count == 1) "1 tarea" else "$count tareas"

private fun pointsLabel(count: Int): String = if (count == 1) "1 punto" else "$count puntos"

/** Aviso de pánico, superpuesto. */
@Composable
private fun PanicOverlay(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AxzyColors.surface,
        shape = AxzyShape.lg,
        title = {
            ITText(text = title, color = AxzyColors.error, style = AxzyType.cardTitle)
        },
        text = {
            ITText(text = message, color = AxzyColors.onSurface, style = AxzyType.cardBody)
        },
        confirmButton = {
            ITButton(label = "Entendido", onClick = onDismiss, compact = true)
        },
    )
}
