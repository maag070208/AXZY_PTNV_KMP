package com.axzydev.checkapp.pages.login.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.BrandMark
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITInlineError
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITCheckbox
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ITWelcomeArt
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyAlpha
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyGradients
import com.axzydev.checkapp.design.theme.AxzyMotion
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.pages.login.viewmodel.LoginAction
import com.axzydev.checkapp.pages.login.viewmodel.LoginUiState

/**
 * Inicio de sesión.
 *
 * Dos piezas y nada más, igual que en la bienvenida:
 *
 * 1. **Campo de marca** (arriba) con el mensaje de valor en blanco y un eco del
 *    motivo de ruta. Es el mismo degradado que Get Started ([AxzyGradients.authRamp]).
 * 2. **Hoja** blanca pegada al borde inferior, con las esquinas de arriba
 *    redondeadas y **alto de su contenido**: el verde asoma por detrás de las curvas.
 *
 * Decisiones que no son evidentes:
 * - La rampa se ancla a una altura ([GRADIENT_RAMP_RATIO]) porque el verde se pinta
 *   detrás de todo: así da igual dónde caiga el borde de la hoja, debajo siempre hay
 *   verde profundo y nunca un corte.
 * - Con el teclado abierto —o en pantallas bajas— la marca se aparta
 *   ([COMPACT_HEIGHT]) y el formulario se queda el alto; si no, el logo se montaba
 *   encima de la hoja y el botón quedaba tapado por el teclado.
 * - El degradado **no** arranca en `brand400`: el blanco encima da 2.60:1 y no pasa
 *   ni el 3:1 de texto grande. `authRamp` arranca en `brand500` (3.23:1).
 * - No hay botones sociales ni "Sign Up": el backend no tiene login social ni
 *   autorregistro. Tampoco "¿Olvidaste tu contraseña?", porque la API sólo expone el
 *   reseteo autenticado de admin. No se pinta una acción que no existe.
 */
@Composable
fun LoginScreen(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = AxzyColors.background) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().imePadding()) {
            val density = LocalDensity.current

            // Entrada orquestada: la hoja sube y aparece. Va en `graphicsLayer`
            // —no en un `offset` ni en `AnimatedVisibility`— para que la métrica
            // de la hoja (alto de su contenido) no cambie durante la animación.
            var entered by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { entered = true }
            val entrance by animateFloatAsState(
                targetValue = if (entered) 1f else 0f,
                animationSpec = tween(durationMillis = AxzyMotion.normal),
                label = "loginSheetEntrance",
            )
            val entranceOffset = with(density) { SHEET_ENTER_OFFSET.toPx() }

            val ramp = remember(maxWidth, maxHeight) {
                with(density) {
                    AxzyGradients.authRamp(
                        endX = maxWidth.toPx(),
                        endY = (maxHeight * GRADIENT_RAMP_RATIO).toPx(),
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().background(ramp))

            // Se mira el inset del teclado y no sólo la altura porque `imePadding`
            // recorta el área de contenido sin cambiar el `maxHeight` de esta caja.
            val compact = WindowInsets.ime.getBottom(density) > 0 || maxHeight < COMPACT_HEIGHT
            // Se calcula aquí y no dentro del `then`: allí el receptor implícito es
            // `ColumnScope`, no el de esta caja.
            val sheetMax = (maxHeight - MIN_BRAND_HEIGHT).coerceAtLeast(MIN_SHEET_HEIGHT)

            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (compact) {
                                Modifier.height(COMPACT_BRAND_HEIGHT)
                            } else {
                                Modifier.weight(1f)
                            },
                        ),
                ) {
                    if (!compact) BrandField()
                }

                LoginSheet(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (compact) {
                                // Con el teclado, la hoja se queda todo el alto que
                                // haya y su contenido hace scroll.
                                Modifier.weight(1f)
                            } else {
                                Modifier.heightIn(max = sheetMax)
                            },
                        )
                        .graphicsLayer {
                            alpha = entrance
                            translationY = (1f - entrance) * entranceOffset
                        },
                )
            }
        }
    }
}

/** Dónde termina la rampa del degradado; de ahí para abajo, verde profundo. */
private const val GRADIENT_RAMP_RATIO = 0.62f

/** Alto que se le reserva a la marca: la hoja nunca se lo come. */
private val MIN_BRAND_HEIGHT = 220.dp

/** Y al revés: la hoja nunca baja de aquí, aunque el teclado apriete. */
private val MIN_SHEET_HEIGHT = 200.dp

/** Por debajo de esta altura disponible (teclado, pantalla corta) manda el formulario. */
private val COMPACT_HEIGHT = 560.dp

/** Con el teclado abierto la marca se queda en un ribete verde. */
private val COMPACT_BRAND_HEIGHT = 96.dp

/** Cuánto sube la hoja en la animación de entrada. */
private val SHEET_ENTER_OFFSET = 24.dp

/**
 * Tamaño de la marca en el campo verde.
 *
 * Sobre blanco opaco y sin teñir: el PNG del logo no tiene transparencia, así que
 * pintarlo de blanco devuelve un cuadrado liso (el mismo detalle que en la
 * cabecera de marca).
 */
private const val BRAND_MARK_SIZE = 56

/** Ancho de la ilustración del campo verde. */
private val ART_WIDTH = 230.dp

/**
 * Pasadas de la animación en el login.
 *
 * Una: monta la escena al entrar y se queda. En bucle permanente distraería a quien
 * está escribiendo sus credenciales, y a partir de la primera pasada no cuenta nada
 * nuevo.
 */
private const val ART_ITERATIONS = 1

/**
 * Campo de marca, en tres franjas: **marca y nombre** arriba, la **ilustración**
 * en medio y el **mensaje** abajo.
 *
 * El campo estaba vacío por el centro —sólo la marca arriba y el titular abajo— y
 * un degradado de media pantalla con dos cosas en los bordes se lee como un hueco,
 * no como una composición. La franja del medio la ocupa la misma animación de ruta
 * que la bienvenida: es el objeto del producto y, al moverse, el hueco deja de
 * estar muerto.
 */
@Composable
private fun BrandField() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = AxzySpacing.xxl, vertical = AxzySpacing.xxl),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BrandLockup()

        // Centrada: el `Column` alinea a la izquierda —que es lo correcto para la
        // marca y el titular— pero la ilustración va en el eje.
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ITWelcomeArt(width = ART_WIDTH, iterations = ART_ITERATIONS)
        }

        BrandHeadline()
    }
}

/**
 * Marca + nombre, en blanco sobre el degradado.
 *
 * El nombre va como texto y no con `BrandWordmark` a propósito: el lockup es un PNG
 * **opaco** de tonos oscuros, así que sobre el verde no se leería (y teñirlo lo
 * convertiría en un cuadrado liso).
 */
@Composable
private fun BrandLockup() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
    ) {
        BrandMark(size = BRAND_MARK_SIZE, background = Color.White)

        ITText(
            text = "CheckApp",
            color = Color.White,
            style = AxzyType.screenTitle,
        )
    }
}

/**
 * Mensaje de valor sobre el degradado.
 *
 * Va abajo a la izquierda, pegado a la hoja, para que haga de encabezado del
 * formulario: el titular centrado sobre un degradado es el recurso por defecto de
 * cualquier plantilla y además deja un hueco raro a los lados.
 */
@Composable
private fun BrandHeadline() {
    Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.xl)) {
        ITText(
            text = "Entra y retoma tu turno donde lo dejaste.",
            color = Color.White,
            style = AxzyType.brandTitle,
        )

        AuthRouteMotif()
    }
}

/**
 * Eco del motivo de ruta: los mismos checkpoints de la bienvenida, en pequeño.
 * Es decorativo y va en `onBrandDivider`, así que no compite con el titular.
 */
@Composable
private fun AuthRouteMotif() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(MOTIF_DOTS) { index ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(MOTIF_CONNECTOR_WIDTH)
                        .height(MOTIF_CONNECTOR_HEIGHT)
                        .background(
                            Color.White.copy(alpha = AxzyAlpha.onBrandDivider),
                            AxzyShape.pill,
                        ),
                )
            }
            Box(
                modifier = Modifier
                    .size(MOTIF_DOT_SIZE)
                    .background(
                        color = if (index < MOTIF_DOTS - 1) {
                            Color.White.copy(alpha = AxzyAlpha.onBrandMuted)
                        } else {
                            Color.White.copy(alpha = AxzyAlpha.onBrandDivider)
                        },
                        shape = AxzyShape.pill,
                    ),
            )
        }
    }
}

/** Hoja inferior con el formulario. */
@Composable
private fun LoginSheet(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ITCard(
        modifier = modifier,
        shape = AxzyShape.sheet,
        contentPadding = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // La hoja llega al borde; el texto no (barra de gestos, notch).
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                    ),
                )
                .padding(horizontal = AxzySpacing.xxl, vertical = AxzySpacing.xxl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Centrado, como en la referencia: es jerarquía, no color.
            ITText(
                text = "Iniciar sesión",
                color = AxzyColors.onSurface,
                style = AxzyType.screenTitle,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(AxzySpacing.xxl))

            ITTextField(
                label = "Usuario",
                value = state.username,
                onValueChange = { onAction(LoginAction.Username(it)) },
                enabled = !state.loading,
                leadingIcon = { FieldIcon(ITIcons.Person) },
                imeAction = ImeAction.Next,
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth(),
                // Sin `error`: el único fallo que puede llegar aquí viene de la API
                // (credenciales, red o límite de intentos), y marcar el campo en rojo
                // culpaba a la contraseña de un problema que no era suyo.
            )

            state.error?.let { message ->
                Spacer(Modifier.height(AxzySpacing.fieldGap))
                ITInlineError(message)
            }

            Spacer(Modifier.height(AxzySpacing.lg))

            // "Recordarme" sí hace algo: con la casilla apagada el token no se
            // persiste y hay que volver a entrar al reiniciar (puesto compartido).
            // Alineada a la izquierda: centrada parecía un botón más.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                ITCheckbox(
                    checked = state.rememberMe,
                    onCheckedChange = { onAction(LoginAction.RememberMe(it)) },
                    label = "Recordarme",
                    enabled = !state.loading,
                )
            }

            Spacer(Modifier.height(AxzySpacing.xxl))

            ITButton(
                label = "Entrar",
                onClick = { onAction(LoginAction.Submit) },
                enabled = state.canSubmit,
                loading = state.loading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(AxzySpacing.xl))

            // `onSurfaceVariant` (slate500, 4.76:1) y no `slate400` (2.56:1, que ni
            // llega al 3:1): era el texto con peor contraste de la pantalla.
            ITText(
                text = "AXZY CHECK · AXZY Digital Systems",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
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

private const val MOTIF_DOTS = 4
private val MOTIF_DOT_SIZE = 8.dp
private val MOTIF_CONNECTOR_WIDTH = 20.dp
private val MOTIF_CONNECTOR_HEIGHT = 2.dp
