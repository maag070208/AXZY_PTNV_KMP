package com.axzydev.checkapp.widgets

import androidx.compose.runtime.Composable
import com.axzydev.checkapp.design.components.ITText

/**
 * Capa FSD `widgets`.
 *
 * Bloques compuestos reutilizables (drawer, tabs, datatable-shell, timelines,
 * secciones de evidencia...). Placeholder de Fase 0 para validar el wiring
 * de Compose entre módulos.
 */
@Composable
fun NotFoundPlaceholder(message: String) {
    ITText(text = message)
}
