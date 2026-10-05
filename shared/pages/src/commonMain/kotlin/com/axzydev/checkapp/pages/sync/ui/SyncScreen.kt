package com.axzydev.checkapp.pages.sync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITFeatureBadge
import com.axzydev.checkapp.design.components.ITFeatureCard
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette
import com.axzydev.checkapp.pages.sync.viewmodel.ServerStatus
import com.axzydev.checkapp.pages.sync.viewmodel.SyncAction
import com.axzydev.checkapp.pages.sync.viewmodel.SyncRunStatus
import com.axzydev.checkapp.pages.sync.viewmodel.SyncStepStatus
import com.axzydev.checkapp.pages.sync.viewmodel.SyncUiState
import com.axzydev.checkapp.pages.sync.viewmodel.UploadProgress

/**
 * Sincronización.
 *
 * Cuando falla hay un botón de reintento: en campo la red va y viene, y obligar a
 * salir y volver a entrar para reintentar es lo que hace que la gente deje de
 * sincronizar y pierda registros.
 */
@Composable
fun SyncScreen(
    state: SyncUiState,
    onAction: (SyncAction) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AxzySpacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ITFeatureCard(height = 180.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ITFeatureBadge(size = 72.dp) {
                    if (state.status == SyncRunStatus.SYNCING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = AxzyColors.primary,
                            strokeWidth = 3.dp,
                        )
                    } else {
                        Icon(
                            imageVector = if (state.status == SyncRunStatus.SUCCESS) {
                                ITIcons.ShieldCheck
                            } else {
                                ITIcons.Warning
                            },
                            contentDescription = null,
                            tint = if (state.status == SyncRunStatus.SUCCESS) {
                                AxzyColors.success
                            } else {
                                AxzyColors.error
                            },
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
                Spacer(Modifier.height(AxzySpacing.md))
                ITText(
                    text = when (state.status) {
                        SyncRunStatus.SYNCING -> "Sincronizando…"
                        SyncRunStatus.SUCCESS -> "Todo al día"
                        SyncRunStatus.ERROR -> "No se pudo sincronizar"
                    },
                    color = androidx.compose.ui.graphics.Color.White,
                    style = AxzyType.cardTitle,
                )
            }
        }

        Spacer(Modifier.height(AxzySpacing.xxl))

        ITCard(modifier = Modifier.fillMaxWidth()) {
            state.steps.forEachIndexed { index, step ->
                SyncStepRow(
                    label = step.label,
                    status = step.status,
                    isLast = index == state.steps.lastIndex,
                )
            }

            state.upload?.let { upload ->
                Spacer(Modifier.height(AxzySpacing.md))
                UploadBar(upload)
            }

            state.error?.let { error ->
                Spacer(Modifier.height(AxzySpacing.md))
                ITText(text = error, color = AxzyColors.error, style = AxzyType.itemMeta)
            }

            Spacer(Modifier.height(AxzySpacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ServerChip(state.serverStatus)
                Spacer(Modifier.weight(1f))
                if (state.status == SyncRunStatus.ERROR) {
                    ITButton(
                        label = "Reintentar",
                        onClick = { onAction(SyncAction.Retry) },
                        compact = true,
                    )
                } else {
                    ITButton(
                        label = "Cerrar",
                        onClick = onDone,
                        outlined = true,
                        tone = Tone.Neutral,
                        compact = true,
                    )
                }
            }
        }
    }
}

/** Un paso del proceso, con su marca de estado. */
@Composable
private fun SyncStepRow(
    label: String,
    status: SyncStepStatus,
    isLast: Boolean,
) {
    val colors = when (status) {
        SyncStepStatus.PENDING -> Tone.Neutral.palette
        SyncStepStatus.ACTIVE -> Tone.Info.palette
        SyncStepStatus.SUCCESS -> Tone.Success.palette
        SyncStepStatus.ERROR -> Tone.Danger.palette
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AxzySpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(colors.soft, AxzyShape.pill),
            contentAlignment = Alignment.Center,
        ) {
            if (status == SyncStepStatus.ACTIVE) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = colors.solid,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(colors.solid, AxzyShape.pill),
                )
            }
        }

        ITText(
            text = label,
            color = when (status) {
                SyncStepStatus.PENDING -> AxzyColors.onSurfaceVariant
                else -> AxzyColors.onSurface
            },
            style = AxzyType.cardBody,
            modifier = Modifier.weight(1f),
        )
    }

    if (!isLast) {
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(1.dp),
        )
    }
}

/** Barra de progreso de la subida de medios. */
@Composable
private fun UploadBar(upload: UploadProgress) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ITText(
            text = "Subiendo archivos: ${upload.current} de ${upload.total}",
            color = AxzyColors.onSurfaceVariant,
            style = AxzyType.itemMeta,
        )
        Spacer(Modifier.height(AxzySpacing.sm))
        LinearProgressIndicator(
            progress = { upload.fraction },
            modifier = Modifier.fillMaxWidth(),
            color = AxzyColors.primary,
            trackColor = AxzyColors.surfaceVariant,
        )
    }
}

/** Estado del servidor, como insignia. */
@Composable
private fun ServerChip(status: ServerStatus) {
    val (label, tone) = when (status) {
        ServerStatus.CHECKING -> "Comprobando" to Tone.Neutral
        ServerStatus.UPDATES_PENDING -> "Cambios pendientes" to Tone.Warning
        ServerStatus.UP_TO_DATE -> "Servidor al día" to Tone.Success
    }
    ITBadge(text = label, tone = tone, dot = true)
}
