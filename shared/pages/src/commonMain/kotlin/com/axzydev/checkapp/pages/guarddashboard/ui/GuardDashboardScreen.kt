package com.axzydev.checkapp.pages.guarddashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axzydev.checkapp.core.common.time.currentGreeting
import com.axzydev.checkapp.design.components.ITActionTile
import com.axzydev.checkapp.design.components.ITAvatar
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITEmptyState
import com.axzydev.checkapp.design.components.ITFeatureBadge
import com.axzydev.checkapp.design.components.ITFeatureCard
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITSectionTitle
import com.axzydev.checkapp.design.components.ITSkeletonList
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.components.MenuButton
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
 * Tres bloques, en orden de urgencia: **quién eres y en qué estado estás**
 * (cabecera), **qué haces ahora** (el bloque oscuro con la acción principal) y
 * **dónde vas después** (accesos). Debajo, sólo lo que el guardia necesita
 * cuando ya no está en la ruta.
 *
 * El bloque oscuro es negro y no verde a propósito: el verde se reserva para los
 * botones que inician la acción, así que el sitio de la acción principal no
 * compite con ella.
 *
 * Antes esta pantalla no tenía estados: con el API caído o sin rutas asignadas
 * pintaba el mismo bloque negro vacío, que es un callejón sin salida — te dice
 * que inicies una ruta sin ofrecer ninguna. Ahora hay esqueleto mientras carga,
 * error con reintento, y "sin rutas" con salida.
 */
@Composable
fun GuardDashboardScreen(
    state: GuardDashboardUiState,
    onAction: (GuardDashboardAction) -> Unit,
    onOpenCheck: (roundId: String, locationId: String) -> Unit,
    onScan: () -> Unit,
    onOpenRound: (roundId: String) -> Unit,
    onOpenKardex: () -> Unit,
    onReportIncident: () -> Unit,
    onReportMaintenance: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onSync: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenMenu: () -> Unit = {},
) {
    // Primer arranque: todavía no hay ni una ruta ni una ronda en curso. Es el
    // único caso en el que un esqueleto es lo correcto; si ya hay datos, un
    // refresco no debe borrar la pantalla.
    val firstLoad = state.loading && state.routes.isEmpty() && !state.hasActiveRound

    Surface(modifier = modifier.fillMaxSize(), color = AxzyColors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            GuardHeader(
                userName = state.userName,
                active = state.hasActiveRound,
                onOpenMenu = onOpenMenu,
                onSync = onSync,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AxzySpacing.xl),
            ) {
                Spacer(Modifier.height(AxzySpacing.md))

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
                            ActiveScannerCard(
                                onScan = onScan,
                                onEnd = { onAction(GuardDashboardAction.EndRound) },
                            )
                        } else {
                            IdleScannerCard(
                                routes = state.routes,
                                onStartRoute = { onAction(GuardDashboardAction.StartRoute(it)) },
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
                            ITSectionTitle(text = "Ruta en curso", action = {
                                ITBadge(text = "${state.points.size} puntos", tone = Tone.Brand)
                            })
                            Spacer(Modifier.height(AxzySpacing.sm))
                            state.points.forEach { point ->
                                PointRow(point = point, onClick = {
                                    state.activeRoundId?.let { id -> onOpenCheck(id, point.locationId) }
                                })
                            }
                        }

                        Spacer(Modifier.height(AxzySpacing.xxl))

                        ITSectionTitle(text = "Acceso")
                        Spacer(Modifier.height(AxzySpacing.sm))

                        AccessRow(
                            title = "Historial",
                            subtitle = "Recorridos anteriores",
                            icon = ITIcons.Route,
                            onClick = onOpenKardex,
                        )
                        AccessRow(
                            title = "Notificaciones",
                            subtitle = "Avisos recibidos",
                            icon = ITIcons.Bell,
                            onClick = onOpenNotifications,
                        )
                        AccessRow(
                            title = "Mi perfil",
                            subtitle = "Cuenta y sesión",
                            icon = ITIcons.Person,
                            onClick = onOpenProfile,
                        )

                        Spacer(Modifier.height(AxzySpacing.md))

                        // Salir no navega: cierra la sesión. Va separado y sin
                        // flecha, para que no se pulse por inercia al bajar la lista.
                        AccessRow(
                            title = "Salir",
                            subtitle = "Cerrar sesión",
                            icon = ITIcons.Logout,
                            tone = Tone.Danger,
                            onClick = onLogout,
                        )
                    }
                }

                Spacer(Modifier.height(AxzySpacing.xxxl))
            }
        }
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
 * Cabecera: menú, saludo, nombre y sincronizar.
 *
 * El saludo es dinámico ("Buenos días / Buenas tardes / Buenas noches") y sale
 * del helper compartido con el inicio del administrador: aquí estaba escrito a
 * mano y a las seis de la tarde seguía diciendo "Buenos días".
 */
@Composable
private fun GuardHeader(
    userName: String,
    active: Boolean,
    onOpenMenu: () -> Unit,
    onSync: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AxzySpacing.lg, vertical = AxzySpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
    ) {
        MenuButton(onClick = onOpenMenu)

        ITAvatar(
            initial = userName.ifBlank { "G" },
            status = if (active) Tone.Success else null,
        )

        Column(modifier = Modifier.weight(1f)) {
            ITText(
                text = currentGreeting(),
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.itemMeta,
            )
            ITText(
                text = userName.ifBlank { "Guardia" },
                color = AxzyColors.onSurface,
                style = AxzyType.cardTitle.copy(fontSize = 20.sp, lineHeight = 26.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (active) {
            ITBadge(text = "En ruta", tone = Tone.Success, dot = true)
        }

        SyncButton(onClick = onSync)
    }
}

/**
 * Sincronizar, con icono en vez de texto.
 *
 * "Sync" en inglés dentro de una app en español, y como botón ancho, competía en
 * peso con el nombre del guardia. El gesto de refrescar se entiende por el icono.
 */
@Composable
private fun SyncButton(onClick: () -> Unit) {
    ITTouchableOpacity(onClick = onClick, role = Role.Button) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(AxzyColors.surfaceVariant, AxzyShape.lg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ITIcons.Repeat,
                contentDescription = "Sincronizar",
                tint = AxzyColors.slate700,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Escáner activo: toca para escanear y finalizar ruta. */
@Composable
private fun ActiveScannerCard(onScan: () -> Unit, onEnd: () -> Unit) {
    ITFeatureCard(height = 200.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AxzySpacing.md),
        ) {
            ITFeatureBadge(size = 72.dp) {
                Icon(
                    imageVector = ITIcons.Search,
                    contentDescription = null,
                    tint = AxzyColors.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
            ITText(
                text = "Toca para escanear código",
                color = Color.White,
                style = AxzyType.cardTitle,
            )
            ITButton(label = "Escanear", onClick = onScan, compact = true)
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
 * Sin ruta activa.
 *
 * Con rutas asignadas, se ofrece la primera para empezar. Sin ninguna, el bloque
 * deja de ser un cartel ("inicia una ruta") y pasa a decir qué falta y cómo
 * resolverlo: sincronizar.
 */
@Composable
private fun IdleScannerCard(
    routes: List<RouteUi>,
    onStartRoute: (String) -> Unit,
    onSync: () -> Unit,
) {
    val hasRoutes = routes.isNotEmpty()

    Column {
        ITFeatureCard(height = if (hasRoutes) 140.dp else 180.dp) {
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
                        "Inicia una ruta para habilitar el escáner"
                    } else {
                        "Sin rutas asignadas"
                    },
                    color = Color.White,
                    style = AxzyType.cardTitle,
                    textAlign = TextAlign.Center,
                )

                if (!hasRoutes) {
                    ITText(
                        text = "Sincroniza para recibir las rondas de hoy",
                        color = Color.White.copy(alpha = AxzyAlpha.onBrandMuted),
                        style = AxzyType.itemMeta,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(AxzySpacing.xs))
                    ITButton(
                        label = "Sincronizar",
                        onClick = onSync,
                        compact = true,
                    )
                }
            }
        }

        if (hasRoutes) {
            Spacer(Modifier.height(AxzySpacing.md))
            routes.forEach { route ->
                ITListItem(
                    title = route.title,
                    subtitle = "${route.pointCount} puntos",
                    leadingIcon = { RowIcon(ITIcons.Route, Tone.Brand) },
                    badge = { Chevron() },
                    onClick = { onStartRoute(route.id) },
                )
            }
        }
    }
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

/** Fila de acceso: icono, título y flecha. */
@Composable
private fun AccessRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tone: Tone = Tone.Neutral,
) {
    // La flecha sólo en las filas que navegan: "Salir" cierra la sesión.
    val trailing: (@Composable () -> Unit)? = if (tone == Tone.Danger) null else ({ Chevron() })

    ITListItem(
        title = title,
        subtitle = subtitle,
        leadingIcon = { RowIcon(icon, tone) },
        badge = trailing,
        onClick = onClick,
    )
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

/** Punto de la ruta activa. */
@Composable
private fun PointRow(point: PointUi, onClick: () -> Unit) {
    ITListItem(
        title = point.name,
        subtitle = if (point.taskCount == 1) "1 tarea" else "${point.taskCount} tareas",
        avatarInitial = point.name,
        avatarStatus = if (point.verified) Tone.Success else Tone.Warning,
        badge = if (point.verified) {
            { ITBadge(text = "Hecho", tone = Tone.Success, dot = true) }
        } else {
            null
        },
        onClick = onClick,
    )
}

/** Aviso de pánico, superpuesto. */
@Composable
private fun PanicOverlay(title: String, message: String, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
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
