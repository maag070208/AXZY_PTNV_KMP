package com.axzydev.checkapp.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Escala tipográfica.
 *
 * Nombres por **función**, no por tamaño: `screenTitle` sobrevive a que mañana
 * el título pase de 24 a 22 px; `titleLarge` no dice nada. Las variantes de
 * Material (`headlineSmall`, `titleMedium`…) siguen existiendo para el código
 * que ya las usa, pero las nuevas pantallas usan estos nombres.
 */
object AxzyType {
    /** Título de pantalla. Ej: "Clientes". */
    val screenTitle = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold)
    /** Subtítulo bajo el título de pantalla. */
    val screenSubtitle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)

    /** Cabecera de tarjeta o sección. */
    val cardTitle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
    /** Texto de apoyo dentro de una tarjeta. */
    val cardBody = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)

    /** Etiqueta en mayúsculas, para encabezados de sección y badges. */
    val label = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
    /** Etiqueta pequeña, para metadatos bajo un valor. */
    val labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp)

    /** Cifra grande de un KPI. */
    val kpiValue = TextStyle(fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
    /** Etiqueta de un KPI. */
    val kpiLabel = TextStyle(fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)

    /** Cuerpo de texto de lectura. */
    val body = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal)
    /** Texto auxiliar atenuado. */
    val muted = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)

    /** Etiqueta de una tarjeta de módulo del inicio. */
    val moduleLabel = TextStyle(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)

    /** Cifra de un tile de estadística. */
    val statValue = TextStyle(fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold)

    /** Etiqueta de un tile de estadística. */
    val statLabel = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)

    /** Encabezado de sección: mayúsculas muy espaciadas. */
    val sectionLabel = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )

    /** Texto de botón. */
    val button = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)

    /** Título de marca, en la cabecera del login. */
    val brandTitle = TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    )

    /** Inicial dentro del avatar de una lista. Grande y muy gruesa. */
    val avatarInitial = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold)

    /** Nombre principal de una tarjeta de lista. */
    val itemTitle = TextStyle(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)

    /** Metadato de una tarjeta de lista (cliente, zona, referencia). */
    val itemMeta = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal)

    /** Acción del pie de una tarjeta. */
    val footerAction = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
}

/** Variantes de Material3 alineadas con la escala de arriba. */
internal val AxzyTypography = Typography(
    headlineSmall = AxzyType.screenTitle,
    titleLarge = AxzyType.cardTitle.copy(fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = AxzyType.cardTitle,
    titleSmall = AxzyType.label,
    bodyLarge = AxzyType.body,
    bodyMedium = AxzyType.cardBody,
    bodySmall = AxzyType.muted,
    labelLarge = AxzyType.button,
    labelMedium = AxzyType.label,
    labelSmall = AxzyType.labelSmall,
)

/**
 * Espaciado. Múltiplos de 4 para que todo se alinee sin cuentas raras.
 *
 * La regla de la app React Native era padding de 16 o 20 en pantalla completa y
 * 16 entre inputs; aquí queda igual pero con nombre.
 */
object AxzySpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp

    /** Padding horizontal estándar de una pantalla. */
    val screenH = 20.dp
    /** Padding vertical estándar de una pantalla. */
    val screenV = 16.dp
    /** Separación entre campos de un formulario. */
    val fieldGap = 16.dp
    /** Separación entre tarjetas de una lista. */
    val cardGap = 12.dp
}

/** Radios de esquina. Cards y modales en 12-16, como en la app React Native. */
object AxzyShape {
    val xs = RoundedCornerShape(6.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
    val xl = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(percent = 50)

    /**
     * Curva inferior ancha, para el bloque de marca del login.
     *
     * Portada de la app React Native, que usa 60 dp en las esquinas de abajo:
     * hace que el degradado no corte en seco contra el fondo blanco.
     */
    val bottomCurve = RoundedCornerShape(bottomStart = 60.dp, bottomEnd = 60.dp)

    /** Tarjeta grande y blanda, como el formulario del login original. */
    val cardLarge = RoundedCornerShape(28.dp)

    /** Botón de acción principal: 56 dp de alto con radio 16. */
    val button = RoundedCornerShape(16.dp)

    /**
     * Bloque destacado oscuro (el escáner, la marca).
     *
     * Radio 32, portado del `headerContainer` de la app React Native: es el
     * elemento con más peso visual de esa pantalla.
     */
    val heroCard = RoundedCornerShape(32.dp)

    /** Tarjeta de acción secundaria: 54 dp de alto con radio 14. */
    val actionTile = RoundedCornerShape(14.dp)
}

/**
 * Elevación. La app es "soft elevated": sombras muy tenues y bordes sutiles en
 * vez de sombras marcadas. Se aplica con `shadow(...)` o con `Card`.
 */
object AxzyElevation {
    val none = 0.dp
    val card = 1.dp
    val raised = 3.dp
    val modal = 8.dp
}

/**
 * Sombras suaves del estilo "premium terso".
 *
 * La app WEB separa las tarjetas con un halo grande y muy tenue, no con una
 * sombra gris marcada. En Compose eso se logra con `Modifier.shadow` y un color
 * de sombra **teñido de slate** en vez del negro por defecto.
 */
object AxzyShadow {
    /** Halo de tarjeta en reposo. */
    val card = 12.dp
    /** Halo al elevarse (hover/press, menús). */
    val raised = 18.dp
    /** Color de sombra: slate-900 al 8 %. El tinte evita el gris sucio. */
    val color = Color(0x140F172A)
}

/** Duraciones de animación, para que no aparezcan números sueltos por ahí. */
object AxzyMotion {
    const val fast = 150
    const val normal = 250
    const val slow = 400
}
