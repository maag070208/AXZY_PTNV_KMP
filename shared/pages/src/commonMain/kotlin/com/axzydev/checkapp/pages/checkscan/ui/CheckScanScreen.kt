package com.axzydev.checkapp.pages.checkscan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITFeatureBadge
import com.axzydev.checkapp.design.components.ITFeatureCard
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.checkscan.viewmodel.CheckScanAction
import com.axzydev.checkapp.pages.checkscan.viewmodel.CheckScanUiState

/**
 * Escáner de código QR.
 *
 * El bloque oscuro es el escáner (mismo lenguaje que la pantalla del guardia en la
 * app original). Si la cámara no está activa, se cae al tecleo del código, que es
 * el plan B en campo cuando el QR está dañado o no hay luz.
 */
@Composable
fun CheckScanScreen(
    state: CheckScanUiState,
    onAction: (CheckScanAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ITScreenScaffold(
        title = "Escanear punto",
        state = ScreenState.Ready,
        modifier = modifier,
        onBack = onBack,
    ) {
        ITFeatureCard(height = 200.dp) {
            androidx.compose.foundation.layout.Column(
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
                    text = if (state.cameraActive) "Apunta al código QR" else "Cámara apagada",
                    color = Color.White,
                    style = AxzyType.cardTitle,
                )
                ITButton(
                    label = if (state.cameraActive) "Detener cámara" else "Activar cámara",
                    onClick = {
                        if (state.cameraActive) onAction(CheckScanAction.StopCamera)
                        else onAction(CheckScanAction.StartCamera)
                    },
                    outlined = true,
                    tone = Tone.Neutral,
                    compact = true,
                )
            }
        }

        Spacer(Modifier.height(AxzySpacing.xl))

        ITCard(modifier = Modifier.fillMaxWidth()) {
            ITText(
                text = "O escribe el código",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.sectionLabel,
            )
            Spacer(Modifier.height(AxzySpacing.md))
            ITTextField(
                label = "Código QR",
                value = state.code,
                onValueChange = { onAction(CheckScanAction.Code(it)) },
                enabled = !state.loading,
            )
            Spacer(Modifier.height(AxzySpacing.lg))
            ITButton(
                label = "Validar código",
                onClick = { onAction(CheckScanAction.Submit) },
                loading = state.loading,
                enabled = !state.loading && state.code.isNotBlank(),
            )
        }

        state.error?.let {
            ITCard(modifier = Modifier.fillMaxWidth()) {
                ITText(text = it, color = AxzyColors.error, style = AxzyType.cardBody)
            }
        }
    }
}
