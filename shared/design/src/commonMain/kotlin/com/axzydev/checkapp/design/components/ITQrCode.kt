package com.axzydev.checkapp.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.axzydev.checkapp.design.qr.QrMatrix

/**
 * Render de un código QR a partir de su contenido. Dibuja la matriz con [Canvas]
 * (multiplataforma, sin dependencias nativas).
 *
 * @param quietZone módulos de margen blanco alrededor (estándar: 4; aquí 2 por defecto).
 */
@Composable
fun ITQrCode(
    content: String,
    modifier: Modifier = Modifier,
    darkColor: Color = Color.Black,
    lightColor: Color = Color.White,
    quietZone: Int = 2,
) {
    val matrix = remember(content) { QrMatrix.of(content) }
    Canvas(modifier = modifier) {
        val modules = matrix.size
        if (modules == 0) return@Canvas
        val total = modules + quietZone * 2
        val cell = size.minDimension / total
        drawRect(color = lightColor, size = Size(cell * total, cell * total))
        for (row in 0 until modules) {
            for (col in 0 until modules) {
                if (matrix[row][col]) {
                    drawRect(
                        color = darkColor,
                        topLeft = Offset((col + quietZone) * cell, (row + quietZone) * cell),
                        size = Size(cell, cell),
                    )
                }
            }
        }
    }
}
