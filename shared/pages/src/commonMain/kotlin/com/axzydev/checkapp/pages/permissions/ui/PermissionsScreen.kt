package com.axzydev.checkapp.pages.permissions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCardFooter
import com.axzydev.checkapp.design.components.ITFooterAction
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSectionTitle
import com.axzydev.checkapp.design.components.ITStatCard
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette
import com.axzydev.checkapp.core.permissions.AppPermission
import com.axzydev.checkapp.core.permissions.PermissionStatus

/**
 * Permisos que la app necesita, con su estado real y la salida cuando falta alguno.
 *
 * **Cada fila dice para qué es.** Un permiso sin motivo es un permiso que se niega:
 * "ubicación" no explica nada, "para dejar constancia de dónde hiciste cada punto"
 * sí. Y cuando el permiso quedó **bloqueado** —el sistema ya no muestra el diálogo—
 * la fila lo dice y ofrece la única salida real, que son los ajustes.
 *
 * Estado por parámetro, sin view-model: el estado **es** el del sistema operativo,
 * que se relee al volver de los ajustes. Guardarlo en un view-model sería una copia
 * que se queda vieja en cuanto el usuario cambia algo fuera de la app.
 */
@Composable
fun PermissionsScreen(
    statuses: Map<AppPermission, PermissionStatus>,
    onRequest: (AppPermission) -> Unit,
    onOpenSettings: () -> Unit,
    /**
     * Volver. `null` en el flujo de entrada: ahí no hay a dónde volver —el login ya
     * se consumió— y una flecha que lleva al inicio es una salida que nadie pidió.
     */
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    /**
     * Continuar sin conceder el resto. Sólo llega en el flujo de entrada: desde el
     * menú la pantalla se visita y se sale, no hay a dónde continuar.
     */
    onContinue: (() -> Unit)? = null,
) {
    val runtime = AppPermission.entries.filter { it.runtime }
    val granted = runtime.count { statuses[it] == PermissionStatus.GRANTED }
    val allGranted = granted == runtime.size
    val tone = if (allGranted) Tone.Success else Tone.Warning

    ITScreenScaffold(
        title = "Permisos",
        state = ScreenState.Ready,
        onBack = onBack,
        // Sin atrás tampoco se hereda el botón del menú: este paso no es una pantalla
        // raíz, y desde aquí no se navega a ningún lado.
        navigationIcon = if (onBack == null) ({}) else null,
        actions = {
            ITBadge(text = "$granted de ${runtime.size}", tone = tone, dot = true)
        },
        modifier = modifier,
    ) {
        ITStatCard(
            label = "Permisos activos",
            value = "$granted de ${runtime.size}",
            hint = if (allGranted) {
                "La app puede registrar todo"
            } else {
                "Falta alguno: la evidencia y la ubicación no se guardarán"
            },
            tone = tone,
            icon = if (allGranted) ITIcons.ShieldCheck else ITIcons.Warning,
        )

        ITSectionTitle(text = "Qué necesita la app")

        AppPermission.entries.forEach { permission ->
            PermissionRow(
                permission = permission,
                status = statuses[permission] ?: PermissionStatus.DENIED,
                onRequest = { onRequest(permission) },
                onOpenSettings = onOpenSettings,
            )
        }

        Spacer(Modifier.height(AxzySpacing.xs))

        ITText(
            text = "Puedes cambiar estos permisos cuando quieras desde los ajustes " +
                "del sistema. Si niegas alguno, la app sigue funcionando: sólo se " +
                "pierden la ubicación y la evidencia de los reportes.",
            color = AxzyColors.onSurfaceVariant,
            style = AxzyType.itemMeta,
        )

        // Conceder es opcional y el botón lo dice: si fuera obligatorio, el guardia
        // quedaría encerrado en una pantalla que puede no querer aceptar.
        onContinue?.let { continueAction ->
            Spacer(Modifier.height(AxzySpacing.xs))
            ITButton(
                label = if (granted == runtime.size) "Continuar" else "Continuar sin ellos",
                onClick = continueAction,
            )
        }
    }
}

/** Una fila por permiso: motivo, estado y la acción que toque. */
@Composable
private fun PermissionRow(
    permission: AppPermission,
    status: PermissionStatus,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val view = permission.present(status)

    // El estado va en el pie y no como insignia al lado del título: la insignia se
    // come el ancho de la fila y el motivo —que es lo que explica para qué se pide—
    // quedaba cortado a media frase.
    ITListItem(
        title = view.title,
        subtitle = view.reason,
        leadingIcon = {
            Icon(
                imageVector = permissionIcon(permission),
                contentDescription = null,
                tint = view.statusTone.palette.solid,
                modifier = Modifier.size(20.dp),
            )
        },
        footer = {
            ITCardFooter {
                // Media fila cada uno: el estado a la izquierda y la acción centrada
                // en su mitad. Sin repartir, la acción queda flotando en el hueco que
                // deja el texto.
                Box(modifier = Modifier.weight(1f)) {
                    ITText(
                        text = view.statusLabel,
                        color = view.statusTone.palette.solid,
                        style = AxzyType.footerAction,
                    )
                }
                view.actionLabel?.let { label ->
                    ITFooterAction(
                        label = label,
                        onClick = { if (view.opensSettings) onOpenSettings() else onRequest() },
                        tone = view.actionTone,
                    )
                }
            }
        },
    )
}

/** Lo que se pinta de un permiso, ya resuelto. */
private data class PermissionView(
    val title: String,
    val reason: String,
    val statusLabel: String,
    val statusTone: Tone,
    val actionLabel: String?,
    val actionTone: Tone,
    val opensSettings: Boolean,
)

/**
 * Traduce el estado del sistema a texto y acción.
 *
 * Los permisos que no se piden (`runtime = false`) salen como "Siempre activo" y sin
 * botón: ofrecer una acción que el sistema no admite es peor que no ofrecerla.
 */
private fun AppPermission.present(status: PermissionStatus): PermissionView {
    val base = when (this) {
        AppPermission.INTERNET -> "Para enviar tus reportes y sincronizar."
        AppPermission.LOCATION -> "Para dejar constancia de cada punto."
        AppPermission.CAMERA -> "Para adjuntar la foto de la evidencia."
        AppPermission.MICROPHONE -> "Para el audio de los vídeos."
    }

    val title = when (this) {
        AppPermission.INTERNET -> "Internet"
        AppPermission.LOCATION -> "Ubicación"
        AppPermission.CAMERA -> "Cámara"
        AppPermission.MICROPHONE -> "Micrófono"
    }

    return when (status) {
        PermissionStatus.GRANTED -> PermissionView(
            title = title,
            reason = base,
            statusLabel = if (runtime) "Concedido" else "Siempre activo",
            statusTone = if (runtime) Tone.Success else Tone.Neutral,
            actionLabel = null,
            actionTone = Tone.Neutral,
            opensSettings = false,
        )

        PermissionStatus.DENIED -> PermissionView(
            title = title,
            reason = base,
            statusLabel = "Sin conceder",
            statusTone = Tone.Warning,
            actionLabel = "Conceder",
            actionTone = Tone.Brand,
            opensSettings = false,
        )

        // El diálogo del sistema ya no vuelve a salir: lo único que queda es Ajustes.
        PermissionStatus.BLOCKED -> PermissionView(
            title = title,
            reason = base,
            statusLabel = "Bloqueado",
            statusTone = Tone.Danger,
            actionLabel = "Abrir ajustes",
            actionTone = Tone.Warning,
            opensSettings = true,
        )

        PermissionStatus.UNAVAILABLE -> PermissionView(
            title = title,
            reason = "Este dispositivo no lo tiene.",
            statusLabel = "No disponible",
            statusTone = Tone.Neutral,
            actionLabel = null,
            actionTone = Tone.Neutral,
            opensSettings = false,
        )
    }
}

private fun permissionIcon(permission: AppPermission): ImageVector = when (permission) {
    AppPermission.INTERNET -> ITIcons.Wifi
    AppPermission.LOCATION -> ITIcons.Place
    AppPermission.CAMERA -> ITIcons.PhotoCamera
    AppPermission.MICROPHONE -> ITIcons.Mic
}
