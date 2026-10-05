package com.axzydev.checkapp.design.components

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing

/**
 * Campo de búsqueda.
 *
 * Las listas usaban un `TextField` de Material con la etiqueta flotante, que en
 * un buscador sobra: el placeholder basta y la etiqueta flotante roba altura.
 * Aquí va sin etiqueta, con forma de pastilla y acción de teclado "buscar".
 */
@Composable
fun ITSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar…",
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        placeholder = {
            ITText(
                text = placeholder,
                color = AxzyColors.onSurfaceVariant,
            )
        },
        // Si no se pide un icono, se pone la lupa: un buscador sin lupa se
        // confunde con un campo de texto normal.
        leadingIcon = leadingIcon ?: {
            Icon(
                imageVector = ITIcons.Search,
                contentDescription = null,
                tint = AxzyColors.slate400,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = trailingIcon,
        shape = AxzyShape.lg,
        textStyle = com.axzydev.checkapp.design.theme.AxzyType.body,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AxzyColors.surfaceVariant,
            unfocusedContainerColor = AxzyColors.surfaceVariant,
            focusedBorderColor = AxzyColors.primary,
            unfocusedBorderColor = AxzyColors.outlineVariant,
            cursorColor = AxzyColors.primary,
        ),
    )
}
