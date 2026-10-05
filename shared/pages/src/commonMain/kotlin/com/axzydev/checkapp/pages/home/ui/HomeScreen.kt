package com.axzydev.checkapp.pages.home.ui

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.axzydev.checkapp.design.components.ITAvatar
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITModuleCard
import com.axzydev.checkapp.design.components.ITSectionTitle
import com.axzydev.checkapp.design.components.ITStatTile
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.MenuButton
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    val tone: Tone,
    val onClick: () -> Unit,
)

/**
 * Inicio del panel administrativo.
 *
 * Rehecho siguiendo el `HomeScreen` de la app React Native: una fila de **tres
 * indicadores** arriba y debajo una **rejilla de tres columnas** con los módulos.
 *
 * Antes era una lista vertical de tarjetas a ancho completo. El cambio no es sólo
 * estético: doce módulos en columna obligan a hacer scroll para ver la mitad,
 * mientras que en rejilla entran casi todos en pantalla. Y las tres cifras de
 * arriba responden a "¿hay algo que atender?" antes de que el usuario toque nada.
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
    // El orden replica el del inicio original: primero lo que se atiende a diario.
    val modules = listOf(
        HomeModule("Usuarios", ITIcons.PersonAdd, Tone.Brand, onOpenUsers),
        HomeModule("Guardias", ITIcons.ShieldCheck, Tone.Brand, onOpenGuards),
        HomeModule("Alertas", ITIcons.Warning, Tone.Danger, onOpenIncidents),
        HomeModule("Mantenimiento", ITIcons.Wrench, Tone.Warning, onOpenMaintenance),
        HomeModule("Quejas Guardias", ITIcons.ErrorOutline, Tone.Accent, onOpenDiscipline),
        HomeModule("Clientes", ITIcons.Business, Tone.Info, onOpenClients),
        HomeModule("Puntos", ITIcons.Place, Tone.Success, onOpenLocations),
        HomeModule("Rutas", ITIcons.Repeat, Tone.Brand, onOpenRecurring),
        HomeModule("Recorridos", ITIcons.Route, Tone.Neutral, onOpenGuardLogs),
        HomeModule("Horarios", ITIcons.Calendar, Tone.Warning, onOpenSchedules),
        HomeModule("Asignaciones", ITIcons.Layers, Tone.Info, onOpenAssignments),
        HomeModule("Notificaciones", ITIcons.Bell, Tone.Info, onOpenNotifications),
    )

    Surface(modifier = modifier.fillMaxSize(), color = AxzyColors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            HomeHeader(userName = userName, roleLabel = roleLabel, onOpenMenu = onOpenMenu)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AxzySpacing.xl),
            ) {
                Spacer(Modifier.height(AxzySpacing.lg))

                StatsRow(
                    activeRounds = activeRounds,
                    pendingIncidents = pendingIncidents,
                    pendingMaintenance = pendingMaintenance,
                )

                Spacer(Modifier.height(AxzySpacing.xxl))

                ITSectionTitle(text = "Accesos rápidos")

                Spacer(Modifier.height(AxzySpacing.md))

                ModuleGrid(modules = modules)

                Spacer(Modifier.height(AxzySpacing.xxxl))
            }
        }
    }
}

/**
 * Cabecera: menú, saludo con el nombre y el rol.
 *
 * El saludo personal es del original y se mantiene: en una app que se abre
 * muchas veces al día, saludar por el nombre es lo que hace que no parezca un
 * formulario.
 */
@Composable
private fun HomeHeader(
    userName: String,
    roleLabel: String,
    onOpenMenu: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AxzySpacing.lg, vertical = AxzySpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
    ) {
        MenuButton(onClick = onOpenMenu)

        ITAvatar(initial = initialsOf(userName), status = Tone.Brand)

        Column(modifier = Modifier.weight(1f)) {
            ITText(
                text = greeting(),
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.itemMeta,
            )
            ITText(
                text = userName.ifBlank { "Usuario" },
                color = AxzyColors.onSurface,
                style = AxzyType.cardTitle.copy(fontSize = 20.sp, lineHeight = 26.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (roleLabel.isNotBlank()) {
            ITBadge(text = roleLabel, tone = Tone.Brand)
        }
    }
}

/** Iniciales del nombre para el avatar de la cabecera. */
private fun initialsOf(name: String): String =
    name.trim().split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "U" }

/** Tres indicadores en fila: lo que hay que atender ahora mismo. */
@Composable
private fun StatsRow(
    activeRounds: Int,
    pendingIncidents: Int,
    pendingMaintenance: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
    ) {
        ITStatTile(
            label = "Rondas",
            value = activeRounds.toString(),
            icon = ITIcons.Walk,
            tone = Tone.Brand,
            modifier = Modifier.weight(1f),
        )
        ITStatTile(
            label = "Alertas",
            value = pendingIncidents.toString(),
            icon = ITIcons.Warning,
            // En cero no hay nada que atender: el tinte de alarma sería ruido.
            tone = if (pendingIncidents > 0) Tone.Danger else Tone.Neutral,
            modifier = Modifier.weight(1f),
        )
        ITStatTile(
            label = "Mantenim.",
            value = pendingMaintenance.toString(),
            icon = ITIcons.Wrench,
            tone = if (pendingMaintenance > 0) Tone.Warning else Tone.Neutral,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Rejilla de tres columnas.
 *
 * Se reparte en filas de tres a mano en lugar de usar `FlowRow`, que sigue siendo
 * experimental: así el reparto es exacto y no depende de una API que puede
 * cambiar.
 */
@Composable
private fun ModuleGrid(modules: List<HomeModule>) {
    Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
        modules.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                row.forEach { module ->
                    ITModuleCard(
                        label = module.label,
                        icon = module.icon,
                        tone = module.tone,
                        onClick = module.onClick,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Huecos vacíos: sin esto, las tarjetas de una fila incompleta se
                // reparten todo el ancho y quedan más grandes que las demás.
                repeat(3 - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Hora local actual.
 *
 * Está aislada en su propia propiedad para poder sustituirla en pruebas: el
 * reloj del sistema no se puede fijar desde un test.
 *
 * En kotlinx-datetime 0.7 `Clock` vive en `kotlin.time`, no en `kotlinx.datetime`.
 */
private val currentHour: Int
    get() = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .hour

/** Saludo según la hora del dispositivo. */
private fun greeting(): String = when (currentHour) {
    in 0..11 -> "Buenos días"
    in 12..18 -> "Buenas tardes"
    else -> "Buenas noches"
}
