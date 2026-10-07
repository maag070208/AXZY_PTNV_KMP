package com.axzydev.checkapp.pages.login.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
import com.axzydev.checkapp.design.theme.AxzyShadow
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette
import com.axzydev.checkapp.pages.login.viewmodel.LoginAction
import com.axzydev.checkapp.pages.login.viewmodel.LoginUiState

/**
 * Inicio de sesión.
 *
 * Dos piezas, y nada más:
 *
 * 1. **Campo de marca**: degradado esmeralda a pantalla completa con el logo,
 *    "CheckApp" y el lema centrados en el verde visible. El bloque va centrado en
 *    esa superficie, no colgado del borde de arriba.
 * 2. **Hoja del formulario**, pegada al borde inferior, con las esquinas de arriba
 *    redondeadas y **alto de su contenido**: el verde asoma por detrás de las
 *    curvas. Antes era una tarjeta flotante con alto fijo, que moría a dos tercios
 *    y dejaba media pantalla de fondo vacío debajo — eso no se lee como diseño,
 *    se lee como maquetación rota.
 *
 * La rampa del degradado se ancla a una altura concreta ([GRADIENT_RAMP_RATIO])
 * porque el verde se pinta detrás de todo: así da igual dónde caiga el borde de la
 * hoja, debajo siempre hay verde profundo y nunca un corte.
 *
 * Con el teclado abierto —o en pantallas muy bajas— la marca se aparta
 * ([COMPACT_HEIGHT]) y el formulario se queda con el alto: si no, el logo se
 * montaba encima de la hoja.
 *
 * Diferencias deliberadas frente a la app de referencia: el formulario tiene
 * scroll (allí el botón quedaba tapado por el teclado) y el pie usa el nombre del
 * producto en lugar del número de versión, que en esta app lo inyecta Gradle.
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
            val ramp = remember(maxWidth, maxHeight) {
                with(density) {
                    AxzyGradients.brandRamp(
                        endX = maxWidth.toPx(),
                        endY = (maxHeight * GRADIENT_RAMP_RATIO).toPx(),
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().background(ramp))

            // El teclado abierto —o una pantalla corta— dejan el alto por debajo
            // de lo que pide la marca: se aparta y el formulario se queda el
            // espacio. Se mira el inset del teclado y no sólo la altura porque
            // `imePadding` recorta el área de contenido sin cambiar el `maxHeight`
            // de esta caja, así que la comprobación por altura no lo detectaba y
            // el logo acababa cortado contra la barra de estado.
            val compact = WindowInsets.ime.getBottom(density) > 0 || maxHeight < COMPACT_HEIGHT
            // Se calcula aquí y no dentro del `then`: en el contenido de la columna
            // el receptor implícito es `ColumnScope`, no el de esta caja.
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
                    contentAlignment = Alignment.Center,
                ) {
                    if (!compact) BrandHeader()
                }

                LoginSheet(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (compact) {
                                // Con el teclado, la hoja se queda todo el alto que
                                // haya y su contenido hace scroll: así el botón se
                                // alcanza siempre.
                                Modifier.weight(1f)
                            } else {
                                // Sin teclado manda el contenido (nada de huecos),
                                // con la marca reservada por arriba.
                                Modifier.heightIn(max = sheetMax)
                            },
                        ),
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

/**
 * Logo, nombre y lema, sobre el degradado.
 *
 * El logo va sobre un cuadrado blanco con sombra: sobre el verde, la marca (que ya
 * es verde) se perdería, y la sombra es lo que lo despega del degradado en vez de
 * dejarlo pegado como una calcomanía.
 */
@Composable
private fun BrandHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(30.dp),
                    clip = false,
                    ambientColor = AxzyShadow.onBrand,
                    spotColor = AxzyShadow.onBrand,
                )
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

/**
 * Hoja inferior con el formulario.
 *
 * Es una tarjeta del sistema con las esquinas de abajo cuadradas y sin padding
 * propio. El alto lo pone el contenido; el scroll sólo entra cuando el alto
 * disponible la obliga a encogerse (teclado abierto), y entonces el pie viaja con
 * el resto en lugar de quedarse cortado.
 */
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
                // El contenido respeta la barra de gestos y, en horizontal, el
                // notch: la hoja sí llega al borde, el texto no.
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                    ),
                )
                .padding(horizontal = AxzySpacing.xxl, vertical = AxzySpacing.xxl)
                .verticalScroll(rememberScrollState()),
        ) {
            // Título y descripción a la izquierda, como en la referencia.
            ITText(
                text = "Iniciar Sesión",
                color = AxzyColors.onSurface,
                style = AxzyType.screenTitle,
            )

            Spacer(Modifier.height(AxzySpacing.sm))

            ITText(
                text = "Ingresa tus credenciales para acceder al sistema",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.cardBody,
            )

            Spacer(Modifier.height(AxzySpacing.xxxl))

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
                // Sin `error`: el único fallo que puede llegar aquí viene de la API
                // (red o credenciales), y marcarlo como error del campo ponía en
                // rojo la contraseña aunque el problema fuera otro.
            )

            state.error?.let { message ->
                Spacer(Modifier.height(AxzySpacing.fieldGap))
                LoginErrorBanner(message)
            }

            Spacer(Modifier.height(AxzySpacing.xxxl))

            ITButton(
                label = "Entrar al Sistema",
                onClick = { onAction(LoginAction.Submit) },
                enabled = state.canSubmit,
                loading = state.loading,
            )

            Spacer(Modifier.height(AxzySpacing.xl))

            ITText(
                text = "AXZY CHECK · AXZY Digital Systems",
                color = AxzyColors.slate400,
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

/**
 * Aviso del formulario, encima del botón.
 *
 * No es el error de un campo: el mensaje que devuelve la API puede ser "usuario o
 * contraseña incorrectos" o un fallo de red, así que va en una banda propia. Antes
 * se pasaba como `error` del campo de contraseña, y con un fallo de red el campo
 * se pintaba en rojo con el volcado de la excepción debajo.
 */
@Composable
private fun LoginErrorBanner(message: String) {
    val colors = Tone.Danger.palette

    Surface(
        color = colors.soft,
        shape = AxzyShape.md,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AxzySpacing.md,
                vertical = AxzySpacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
        ) {
            Icon(
                imageVector = ITIcons.Warning,
                contentDescription = null,
                tint = colors.onSoft,
                modifier = Modifier.size(18.dp),
            )
            ITText(
                text = message,
                color = colors.onSoft,
                style = AxzyType.cardBody,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
