package com.axzydev.checkapp.pages.guarddashboard.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axzydev.checkapp.design.components.ITActionTile
import com.axzydev.checkapp.design.components.ITAvatar
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITFeatureBadge
import com.axzydev.checkapp.design.components.ITFeatureCard
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITSectionTitle
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.MenuButton
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.components.toneForStatus
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.GuardDashboardAction
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.GuardDashboardUiState
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.PointUi
import com.axzydev.checkapp.pages.guarddashboard.viewmodel.RouteUi

/**
 * Panel del guardia.
 *
 * Sigue el diseño de la pantalla del guardia de la app React Native:
 *
 * - Bloque oscuro para la acción principal (escanear / iniciar ruta).
 * - Dos acciones rápidas tintadas (incidencia, mantenimiento).
 * - Rutas asignadas como lista con el número de puntos.
 *
 * El escáner es negro y no verde a propósito: el verde se reserva para el botón
 * que **inicia la acción**, y así el bloque no compite con él.
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

                if (state.hasActiveRound) {
                    ActiveScannerCard(onScan = onScan, onEnd = { onAction(GuardDashboardAction.EndRound) })
                } else {
                    IdleScannerCard(
                        routes = state.routes,
                        onStartRoute = { onAction(GuardDashboardAction.StartRoute(it)) },
                    )
                }

                Spacer(Modifier.height(AxzySpacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                ) {
                    ITActionTile(
                        label = "Incidencia",
                        onClick = onReportIncident,
                        tone = Tone.Danger,
                        modifier = Modifier.weight(1f),
                    )
                    ITActionTile(
                        label = "Mantenimiento",
                        onClick = onReportMaintenance,
                        tone = Tone.Warning,
                        modifier = Modifier.weight(1f),
                    )
                }

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
                ITListItem(title = "Historial", subtitle = "Recorridos anteriores", avatarInitial = "H", avatarStatus = Tone.Info, onClick = onOpenKardex)
                ITListItem(title = "Notificaciones", subtitle = "Avisos recibidos", avatarInitial = "N", avatarStatus = Tone.Info, onClick = onOpenNotifications)
                ITListItem(title = "Mi perfil", subtitle = "Cuenta y sesión", avatarInitial = "P", avatarStatus = Tone.Neutral, onClick = onOpenProfile)
                ITListItem(
                    title = "Salir",
                    subtitle = "Cerrar sesión",
                    avatarInitial = "S",
                    avatarStatus = Tone.Danger,
                    onClick = onLogout,
                )

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

/** Cabecera con saludo, indicador de ruta y sincronización. */
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
            status = if (active) Tone.Success else Tone.Neutral,
        )

        Column(modifier = Modifier.weight(1f)) {
            ITText(
                text = "Buenos días",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.itemMeta,
            )
            ITText(
                text = userName.ifBlank { "Guardia" },
                color = AxzyColors.onSurface,
                style = AxzyType.cardTitle.copy(fontSize = 20.sp, lineHeight = 26.sp),
            )
        }

        if (active) {
            ITBadge(text = "En ruta", tone = Tone.Success, dot = true)
        }

        ITButton(
            label = "Sync",
            onClick = onSync,
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
        )
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

/** Sin ruta activa: se ofrecen las rutas asignadas para empezar. */
@Composable
private fun IdleScannerCard(routes: List<RouteUi>, onStartRoute: (String) -> Unit) {
    Column {
        ITFeatureCard(height = 140.dp) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AxzySpacing.md),
            ) {
                ITFeatureBadge(size = 64.dp) {
                    Icon(
                        imageVector = ITIcons.Walk,
                        contentDescription = null,
                        tint = AxzyColors.primary,
                        modifier = Modifier.size(30.dp),
                    )
                }
                ITText(
                    text = "Inicia una ruta para habilitar el escáner",
                    color = Color.White,
                    style = AxzyType.cardTitle,
                )
            }
        }

        if (routes.isNotEmpty()) {
            Spacer(Modifier.height(AxzySpacing.md))
            routes.forEach { route ->
                ITListItem(
                    title = route.title,
                    subtitle = "${route.pointCount} puntos",
                    avatarInitial = route.title,
                    avatarStatus = Tone.Brand,
                    onClick = { onStartRoute(route.id) },
                )
            }
        }
    }
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
