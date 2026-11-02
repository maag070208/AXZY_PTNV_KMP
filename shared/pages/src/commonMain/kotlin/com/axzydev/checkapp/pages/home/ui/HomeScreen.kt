package com.axzydev.checkapp.pages.home.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.core.common.time.currentGreeting
import com.axzydev.checkapp.design.components.ITBrandHero
import com.axzydev.checkapp.design.components.ITHeroIconButton
import com.axzydev.checkapp.design.components.ITHeroMetric
import com.axzydev.checkapp.design.components.ITModuleCard
import com.axzydev.checkapp.design.components.ITSectionTitle
import com.axzydev.checkapp.design.components.MenuBars
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.Tone

/**
 * Un módulo del inicio.
 *
 * `tone` en vez de un color crudo: la pantalla declara **qué** es cada módulo
 * (peligro, aviso, información) y el sistema decide el color. Así el mismo
 * módulo no sale de un color aquí y de otro en el menú lateral.
 */
private data class HomeModule(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val tone: Tone = Tone.Brand,
)

/** Un bloque de módulos, con el criterio que los agrupa. */
private data class HomeSection(
    val title: String,
    val modules: List<HomeModule>,
)

/**
 * Inicio del panel administrativo.
 *
 * Tres bloques: **quién eres y cómo va el día** (cabecera de marca con el pulso
 * operativo), **qué se atiende ahora** (operación) y **qué se administra**
 * (supervisión, planificación y catálogos). Cada grupo va de tres en tres porque
 * son tres columnas: una fila = un grupo, y así la rejilla no deja tarjetas
 * sueltas colgando.
 *
 * Cambios frente a la versión anterior:
 * - La cabecera es la de marca ([ITBrandHero]) y lleva dentro las tres cifras.
 *   Antes era una fila blanca con un avatar gris —indistinguible de cualquier
 *   app de ajustes— y los indicadores iban en tres tarjetas aparte: dos bloques
 *   para decir lo mismo, y ninguno decía "CheckApp".
 * - Los doce módulos iban en una rejilla plana, con un color distinto cada uno
 *   (siete tonos para doce tarjetas). Un color por módulo no codifica nada: es
 *   decoración. Ahora el color significa **atención** — sólo Alertas y
 *   Mantenimiento salen teñidos, y sólo cuando tienen algo pendiente; el resto va
 *   en el verde de marca.
 */
@Composable
fun HomeScreen(
    userName: String,
    roleLabel: String,
    onOpenClients: () -> Unit,
    onOpenLocations: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenGuards: () -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenSchedules: () -> Unit,
    onOpenRecurring: () -> Unit,
    onOpenIncidents: () -> Unit,
    onOpenMaintenance: () -> Unit,
    onOpenDiscipline: () -> Unit,
    onOpenGuardLogs: () -> Unit,
    onOpenSupervision: () -> Unit,
    onOpenShiftHandover: () -> Unit,
    onOpenBulkPrint: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSendNotification: () -> Unit,
    onOpenScheduledNotifications: () -> Unit,
    onLogout: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
    activeRounds: Int = 0,
    pendingIncidents: Int = 0,
    pendingMaintenance: Int = 0,
) {
    val sections = listOf(
        HomeSection(
            title = "Operación",
            modules = listOf(
                HomeModule(
                    label = "Alertas",
                    icon = ITIcons.Warning,
                    onClick = onOpenIncidents,
                    // El tono sólo cuando hay algo: en cero, rojo es ruido.
                    tone = if (pendingIncidents > 0) Tone.Danger else Tone.Brand,
                ),
                HomeModule(
                    label = "Mantenimiento",
                    icon = ITIcons.Wrench,
                    onClick = onOpenMaintenance,
                    tone = if (pendingMaintenance > 0) Tone.Warning else Tone.Brand,
                ),
                HomeModule("Recorridos", ITIcons.Route, onOpenGuardLogs),
            ),
        ),
        HomeSection(
            title = "Supervisión",
            modules = listOf(
                HomeModule("Quejas Guardias", ITIcons.ErrorOutline, onOpenDiscipline),
                HomeModule("Guardias", ITIcons.ShieldCheck, onOpenGuards),
                HomeModule("Horarios", ITIcons.Calendar, onOpenSchedules),
            ),
        ),
        HomeSection(
            title = "Planificación",
            modules = listOf(
                HomeModule("Rutas", ITIcons.Repeat, onOpenRecurring),
                HomeModule("Asignaciones", ITIcons.Layers, onOpenAssignments),
                HomeModule("Puntos", ITIcons.Place, onOpenLocations),
            ),
        ),
        HomeSection(
            title = "Administración",
            modules = listOf(
                HomeModule("Clientes", ITIcons.Business, onOpenClients),
                HomeModule("Usuarios", ITIcons.PersonAdd, onOpenUsers),
                HomeModule("Notificaciones", ITIcons.Bell, onOpenNotifications),
            ),
        ),
    )

    Surface(modifier = modifier.fillMaxSize(), color = AxzyColors.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeHero(
                userName = userName,
                roleLabel = roleLabel,
                activeRounds = activeRounds,
                pendingIncidents = pendingIncidents,
                pendingMaintenance = pendingMaintenance,
                onOpenMenu = onOpenMenu,
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
                sections.forEach { section ->
                    Spacer(Modifier.height(AxzySpacing.xxl))
                    ITSectionTitle(text = section.title)
                    Spacer(Modifier.height(AxzySpacing.md))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                    ) {
                        section.modules.forEach { module ->
                            ITModuleCard(
                                label = module.label,
                                icon = module.icon,
                                tone = module.tone,
                                onClick = module.onClick,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(AxzySpacing.xxxl))
            }
        }
    }
}

/**
 * Cabecera de marca con el pulso del día.
 *
 * Las tres cifras van dentro del bloque verde y no en tarjetas aparte: responden
 * a "¿hay algo que atender?" antes de que el usuario toque nada, y así ocupan el
 * sitio que ya está pagado con la cabecera en vez de empujar los módulos hacia
 * abajo. La misma cabecera que el panel del guardia: las dos apps se reconocen
 * como la misma.
 */
@Composable
private fun HomeHero(
    userName: String,
    roleLabel: String,
    activeRounds: Int,
    pendingIncidents: Int,
    pendingMaintenance: Int,
    onOpenMenu: () -> Unit,
) {
    ITBrandHero(
        title = userName.ifBlank { "Usuario" },
        subtitle = listOf(currentGreeting(), roleLabel)
            .filter { it.isNotBlank() }
            .joinToString(" · "),
        compact = true,
        actions = {
            ITHeroIconButton(contentDescription = "Abrir menú", onClick = onOpenMenu) {
                MenuBars(tint = Color.White)
            }
        },
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                ITHeroMetric(
                    label = "Rondas",
                    value = activeRounds.toString(),
                    modifier = Modifier.weight(1f),
                )
                ITHeroMetric(
                    label = "Alertas",
                    value = pendingIncidents.toString(),
                    modifier = Modifier.weight(1f),
                )
                ITHeroMetric(
                    label = "Mant.",
                    value = pendingMaintenance.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        },
    )
}

