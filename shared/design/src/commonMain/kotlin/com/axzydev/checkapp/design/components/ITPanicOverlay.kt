package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Overlay de pánico a pantalla completa. Muestra el resultado del disparo
 * (enviado / encolado / error) y permite cerrarlo.
 */
@Composable
fun ITPanicOverlay(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFB91C1C))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "🚨",
            style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
        )
        Text(
            text = title,
            color = Color.White,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            color = Color(0xFFFEE2E2),
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        Button(onClick = onDismiss, modifier = Modifier.padding(top = 28.dp)) {
            Text("Entendido")
        }
    }
}
