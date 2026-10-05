package com.axzydev.checkapp.pages.login.ui

import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.BrandLogo
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyAlpha
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyGradients
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.pages.login.viewmodel.LoginAction
import com.axzydev.checkapp.pages.login.viewmodel.LoginUiState

/**
 * Inicio de sesión.
 *
 * Rehecho para calcar la pantalla de la app React Native, que es la referencia
 * visual acordada. La composición es la misma:
 *
 * 1. Cabecera con degradado verde de marca (algo más de un tercio de la altura).
 * 2. El logo dentro de un **cuadrado blanco redondeado** con sombra, centrado.
 * 3. "CheckApp" y el lema, en blanco y centrados.
 * 4. Una **tarjeta blanca que solapa el degradado** (por eso sus esquinas
 *    superiores son las que redondean la transición, no el degradado).
 * 5. Título y descripción **centrados**, campos con icono y botón verde.
 *
 * Diferencias deliberadas frente al original: se añade scroll e `imePadding`
 * (allí el botón quedaba tapado por el teclado) y el pie usa el nombre del
 * producto en lugar del número de versión, que en esta app lo inyecta Gradle.
 */
@Composable
fun LoginScreen(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = AxzyColors.surface) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Cabecera de marca. El degradado llega hasta detrás de la tarjeta:
            // es la tarjeta la que crea la curva, como en la app original.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(AxzyGradients.brand),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandHeader()

                // La tarjeta sube sobre el degradado con margen negativo.
                Box(modifier = Modifier.padding(horizontal = AxzySpacing.lg)) {
                    LoginCard(state = state, onAction = onAction)
                }

                Spacer(Modifier.height(AxzySpacing.lg))

                ITText(
                    text = "AXZY CHECK · AXZY Digital Systems",
                    color = AxzyColors.slate400,
                    style = AxzyType.labelSmall,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(AxzySpacing.xxl))
            }
        }
    }
}

/**
 * Logo y nombre sobre el degradado.
 *
 * El logo va sobre un cuadrado blanco: sobre el verde, el escudo (que ya es
 * verde) se perdería. Es el mismo recurso que se usa en la barra de la app y en
 * el lanzador, así que la marca es consistente en los tres sitios.
 */
@Composable
private fun BrandHeader() {
    Column(
        modifier = Modifier.padding(top = AxzySpacing.xxxl, bottom = AxzySpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.White, RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center,
        ) {
            BrandLogo(size = 92)
        }

        Spacer(Modifier.height(AxzySpacing.xl))

        ITText(
            text = "CheckApp",
            color = Color.White,
            style = AxzyType.brandTitle,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(AxzySpacing.xs))

        ITText(
            text = "Sistema de Gestión y Control Administrativo",
            color = Color.White.copy(alpha = AxzyAlpha.onBrandMuted),
            style = AxzyType.itemMeta,
            textAlign = TextAlign.Center,
        )
    }
}

/** Tarjeta del formulario, con esquinas superiores grandes. */
@Composable
private fun LoginCard(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
) {
    ITCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ITText(
                text = "Iniciar Sesión",
                color = AxzyColors.onSurface,
                style = AxzyType.screenTitle,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITText(
                text = "Ingresa tus credenciales para acceder al sistema",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.cardBody,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(AxzySpacing.xxl))

        ITTextField(
            label = "Usuario",
            value = state.username,
            onValueChange = { onAction(LoginAction.Username(it)) },
            enabled = !state.loading,
            leadingIcon = { FieldIcon(ITIcons.Person) },
            imeAction = ImeAction.Next,
        )

        Spacer(Modifier.height(AxzySpacing.fieldGap))

        ITTextField(
            label = "Contraseña",
            value = state.password,
            onValueChange = { onAction(LoginAction.Password(it)) },
            enabled = !state.loading,
            isPassword = true,
            leadingIcon = { FieldIcon(ITIcons.Lock) },
            imeAction = ImeAction.Done,
            error = state.error,
        )

        Spacer(Modifier.height(AxzySpacing.xxl))

        ITButton(
            label = "Entrar al Sistema",
            onClick = { onAction(LoginAction.Submit) },
            enabled = state.canSubmit,
            loading = state.loading,
        )
    }
}

/** Icono de campo: persona para el usuario, candado para la contraseña. */
@Composable
private fun FieldIcon(vector: androidx.compose.ui.graphics.vector.ImageVector) {
    Icon(
        imageVector = vector,
        contentDescription = null,
        tint = AxzyColors.slate400,
        modifier = Modifier.size(20.dp),
    )
}
