package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette
import kotlinx.coroutines.launch

/**
 * Una entrada del menú lateral.
 *
 * Es sólo presentación: el módulo de diseño no conoce los roles ni las rutas.
 * Quien construye la lista decide qué ve cada rol, y así la capa de diseño no
 * depende de `entities`.
 */
data class ITNavItem(
    val label: String,
    val section: String,
    val selected: Boolean,
    val onClick: () -> Unit,
    /** Icono de la entrada. */
    val icon: ImageVector? = null,
    /** Texto corto a la derecha (un contador, un estado). */
    val trailing: String? = null,
)

/**
 * Menú lateral.
 *
 * En la app original el drawer era la única forma de navegar entre los ~15
 * módulos, y todo estaba en una lista plana: encontrar "Zonas" obligaba a
 * recorrerla entera. Aquí las entradas se agrupan por `section` y llevan icono,
 * que es lo que permite leer el menú en lugar de escanearlo.
 *
 * El `footer` queda **fijo abajo** (fuera del scroll) para que "Cerrar sesión"
 * siempre esté a la mano, sin desplazar toda la lista.
 *
 * `onClose` se llama al elegir una entrada para que el drawer se cierre solo.
 */
@Composable
fun ITNavigationDrawer(
    items: List<ITNavItem>,
    isOpen: Boolean,
    onClose: () -> Unit,
    header: @Composable () -> Unit,
    footer: (@Composable () -> Unit)? = null,
    /** Contenido de la app. Va al final para poder pasarse como trailing lambda. */
    content: @Composable () -> Unit,
) {
    val drawerState = rememberDrawerState(if (isOpen) DrawerValue.Open else DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = AxzyColors.surface,
                drawerShape = AxzyShape.xl,
                modifier = Modifier.width(300.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars),
                ) {
                    header()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        var currentSection: String? = null
                        items.forEach { item ->
                            if (item.section != currentSection) {
                                currentSection = item.section
                                SectionLabel(item.section)
                            }
                            NavRow(item = item, onSelect = {
                                item.onClick()
                                onClose()
                            })
                        }
                        Spacer(Modifier.height(AxzySpacing.lg))
                    }

                    footer?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(AxzyColors.outlineVariant),
                        )
                        it()
                    }
                }
            }
        },
    ) {
        content()
    }

    // El estado del drawer lo controla quien lo usa; se sincroniza aquí.
    androidx.compose.runtime.LaunchedEffect(isOpen) {
        scope.launch {
            if (isOpen) drawerState.open() else drawerState.close()
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    ITText(
        text = text.uppercase(),
        color = AxzyColors.onSurfaceVariant,
        style = AxzyType.label,
        modifier = Modifier.padding(
            start = AxzySpacing.xl,
            end = AxzySpacing.lg,
            top = AxzySpacing.lg,
            bottom = AxzySpacing.xs,
        ),
    )
}

@Composable
private fun NavRow(item: ITNavItem, onSelect: () -> Unit) {
    val colors = Tone.Brand.palette

    ITTouchableOpacity(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AxzySpacing.md, vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (item.selected) colors.soft else AxzyColors.surface,
                    shape = AxzyShape.md,
                )
                .border(
                    width = 1.dp,
                    color = if (item.selected) colors.border else AxzyColors.surface,
                    shape = AxzyShape.md,
                )
                .padding(horizontal = AxzySpacing.md, vertical = AxzySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
        ) {
            if (item.icon != null) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (item.selected) colors.solid else AxzyColors.slate500,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (item.selected) colors.solid else AxzyColors.outline,
                                shape = AxzyShape.pill,
                            ),
                    )
                }
            }

            ITText(
                text = item.label,
                color = if (item.selected) colors.onSoft else AxzyColors.onSurface,
                style = if (item.selected) AxzyType.cardTitle else AxzyType.cardBody,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            item.trailing?.let {
                ITBadge(text = it, tone = Tone.Neutral)
            }
        }
    }
}
