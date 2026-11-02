package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.generated.resources.Res
import com.axzydev.checkapp.design.generated.resources.checkapp_mark
import com.axzydev.checkapp.design.generated.resources.checkapp_wordmark
import com.axzydev.checkapp.design.generated.resources.checkapp_wordmark_light
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.isDarkMode
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import org.jetbrains.compose.resources.painterResource

/**
 * Barra superior de la app.
 *
 * Antes cada pantalla empezaba con un `ITText` de título metido dentro de la
 * lista, así que el título se desplazaba al hacer scroll y no había forma de
 * saber en qué pantalla estabas. Esta barra es fija y la comparten todas.
 *
 * La marca se pinta con el logo real, no con texto: es lo que hace que la app
 * se reconozca como CheckApp desde la primera pantalla.
 */
@Composable
fun ITAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AxzyColors.surface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = AxzySpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
        ) {
            when {
                navigationIcon != null -> navigationIcon()
                onBack != null -> IconButton(onClick = onBack) {
                    Icon(
                        imageVector = ITIcons.ArrowBack,
                        contentDescription = "Volver",
                        tint = AxzyColors.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            BrandMark()

            Column(modifier = Modifier.weight(1f)) {
                ITText(
                    text = title,
                    color = AxzyColors.onSurface,
                    style = AxzyType.cardTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    ITText(
                        text = subtitle,
                        color = AxzyColors.onSurfaceVariant,
                        style = AxzyType.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            actions?.invoke()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AxzyColors.outlineVariant),
        )
    }
}

/**
 * Símbolo de la marca en un cuadrado redondeado.
 *
 * Se usa la marca real (`checkapp-mark.svg`) y no una letra: es lo que hace que
 * la cabecera se reconozca como CheckApp, y es la misma imagen que el icono del
 * lanzador. Si el recurso no estuviera disponible, cae a las iniciales.
 *
 * `background` y `contentColor` se parametrizan porque la marca aparece tanto
 * sobre fondo blanco (barra de la app) como sobre el degradado verde (cabecera
 * de marca), y en cada sitio necesita contrastar al revés.
 */
@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Int = 32,
    background: Color = AxzyColors.surfaceVariant,
    contentColor: Color = Color.Unspecified,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(background, AxzyShape.sm),
        contentAlignment = Alignment.Center,
    ) {
        BrandLogo(
            size = (size * 0.72).toInt(),
            tint = contentColor,
        )
    }
}

/**
 * Marca de CheckApp, sin el cuadro de fondo.
 *
 * Se expone desde el módulo de diseño porque la clase de recursos generada no es
 * transitiva: si una pantalla de `pages` pidiera `Res.drawable.checkapp_mark`
 * directamente, compilaría pero fallaría al cargar el recurso.
 */
@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    size: Int = 48,
    tint: Color = Color.Unspecified,
) {
    // PNG y no SVG: Compose Resources no soporta SVG en Android.
    // Es la marca de la WEB (QR con pin), no la de la app React Native.
    Icon(
        painter = painterResource(Res.drawable.checkapp_mark),
        contentDescription = "CheckApp",
        tint = tint,
        modifier = modifier.size(size.dp),
    )
}

/**
 * Lockup horizontal de CheckApp: marca + "Check" + "App".
 *
 * Es el logo completo de la WEB. Se usa donde hay sitio para el nombre, como la
 * cabecera del menú lateral; en la barra de la app va sólo la marca, porque el
 * título de la pantalla ya está al lado.
 *
 * `height` controla el alto del **logo**, no el del lienzo, y el ancho se calcula
 * solo. **El `aspectRatio` no es decorativo**: sin él, `Icon` metía el lockup en una
 * caja cuadrada y salía dibujado a un tercio de su ancho real.
 */
@Composable
fun BrandWordmark(
    modifier: Modifier = Modifier,
    height: Int = 32,
) {
    // En oscuro se usa la variante de **tinta clara**: el PNG normal trae "Check" en
    // azul oscuro y sobre una superficie oscura desaparece. Los dos assets salen de
    // `scripts/generate_brand_dark_assets.py`, que además les quitó el fondo blanco
    // opaco (era el recorte blanco que se veía sobre el tema oscuro).
    val asset = if (isDarkMode) {
        Res.drawable.checkapp_wordmark_light
    } else {
        Res.drawable.checkapp_wordmark
    }

    Icon(
        painter = painterResource(asset),
        contentDescription = "CheckApp",
        tint = Color.Unspecified,
        modifier = modifier
            .height(height.dp)
            .aspectRatio(WORDMARK_ASPECT_RATIO),
    )
}

/**
 * Relación del lockup: 972x265 px.
 *
 * El PNG venía del kit de marca como un lienzo **1024x1024** con el logo en una
 * franja central, y eso rompía `height`: se aplicaba al lienzo, así que pedir 72 dp
 * daba un logo visible de ~19 dp. `scripts/normalize_brand_assets.py` recorta el
 * relleno y deja este tamaño; si se reemplaza el asset hay que volver a recortarlo.
 */
private const val WORDMARK_ASPECT_RATIO = 972f / 265f
