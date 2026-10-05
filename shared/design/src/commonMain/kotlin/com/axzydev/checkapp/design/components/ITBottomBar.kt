package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * Una pestaña de la barra inferior.
 *
 * `onClick` va al final para que se pueda pasar como trailing lambda, y el
 * icono queda como parámetro con nombre.
 */
data class ITTab(
    val label: String,
    val selected: Boolean,
    /** Icono de la pestaña; si falta, sólo se ve la etiqueta. */
    val icon: ImageVector? = null,
    val onClick: () -> Unit,
)

/**
 * Barra inferior de pestañas.
 *
 * La app original sólo tenía dos ("Inicio" e "Historial") y aun así eran el
 * único acceso rápido a lo más usado. Se mantiene esa idea: pocas pestañas y
 * muy claras, con el resto de módulos en el menú lateral.
 *
 * `windowInsetsPadding(navigationBars)` es obligatorio: sin él la barra queda
 * debajo de la barra de gestos del sistema y no se puede pulsar la última fila.
 */
@Composable
fun ITBottomBar(
    tabs: List<ITTab>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AxzyColors.surface)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AxzyColors.outlineVariant),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AxzySpacing.sm, vertical = AxzySpacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab -> TabItem(tab = tab, modifier = Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun TabItem(tab: ITTab, modifier: Modifier = Modifier) {
    val color = if (tab.selected) Tone.Brand.palette.solid else AxzyColors.onSurfaceVariant

    ITTouchableOpacity(
        onClick = tab.onClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AxzySpacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AxzySpacing.xs),
        ) {
            if (tab.icon != null) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp),
                )
            }

            ITText(
                text = tab.label,
                color = color,
                style = AxzyType.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
