package com.axzydev.checkapp.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType

/**
 * Campo de texto del sistema.
 *
 * Existe porque el mismo `OutlinedTextField` estaba copiado en cada pantalla con
 * detalles distintos (etiqueta de Material, sin forma, sin color de foco), así
 * que dos formularios nunca se veían igual. Aquí van juntos el estilo, el error
 * y el tipo de teclado.
 *
 * El mensaje de error va **debajo del campo y en rojo**, no sustituyendo a la
 * etiqueta: así el usuario ve qué campo falla sin perder de vista qué escribía.
 */
@Composable
fun ITTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    numeric: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val hasError = error != null

    // El ojo para mostrar la contraseña: en la app original está, y es lo que
    // evita que la gente escriba a ciegas en el móvil.
    var visible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            isError = hasError,
            label = {
                ITText(
                    text = label,
                    style = AxzyType.labelSmall,
                    color = if (hasError) AxzyColors.error else AxzyColors.onSurfaceVariant,
                )
            },
            placeholder = placeholder?.let {
                { ITText(text = it, color = AxzyColors.onSurfaceVariant, style = AxzyType.cardBody) }
            },
            visualTransformation = if (isPassword && !visible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            leadingIcon = leadingIcon?.let {
                {
                    Box(
                        modifier = Modifier
                            .padding(start = AxzySpacing.md, end = AxzySpacing.xs),
                        contentAlignment = Alignment.Center,
                    ) { it() }
                }
            },
            trailingIcon = if (isPassword) {
                {
                    // Icono de ojo, no la palabra "Ver": ocupa menos y se
                    // entiende sin leer.
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            imageVector = if (visible) ITIcons.EyeOff else ITIcons.Eye,
                            contentDescription = if (visible) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            },
                            tint = AxzyColors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text,
                imeAction = imeAction,
            ),
            shape = AxzyShape.sm,
            textStyle = AxzyType.body,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AxzyColors.primary,
                unfocusedBorderColor = AxzyColors.outlineVariant,
                errorBorderColor = AxzyColors.error,
                focusedLabelColor = AxzyColors.primary,
                cursorColor = AxzyColors.primary,
                focusedContainerColor = AxzyColors.surface,
                unfocusedContainerColor = AxzyColors.surface,
            ),
        )

        if (hasError) {
            ITText(
                text = error,
                color = AxzyColors.error,
                style = AxzyType.labelSmall,
                modifier = Modifier.padding(start = AxzySpacing.sm, top = AxzySpacing.xs),
            )
        }
    }
}
