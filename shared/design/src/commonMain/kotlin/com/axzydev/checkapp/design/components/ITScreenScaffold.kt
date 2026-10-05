package com.axzydev.checkapp.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing

/**
 * Estado de una pantalla. Obliga a declarar **cuál** de los cuatro casos se está
 * pintando.
 *
 * El problema que resuelve: en el código original cada pantalla decidía por su
 * cuenta qué hacer mientras cargaba o si fallaba, así que una lista vacía, una
 * carga en curso y un error de red se veían los tres igual — como nada. Con
 * esto, la pantalla no puede "olvidarse" de un estado.
 */
sealed interface ScreenState {
    /** Cargando por primera vez: se pinta el esqueleto. */
    data object Loading : ScreenState

    /** Hay datos (o la pantalla no depende de datos). */
    data object Ready : ScreenState

    /** Cargó bien pero no hay nada que mostrar. */
    data class Empty(
        val title: String,
        val description: String? = null,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null,
    ) : ScreenState

    /** Falló: se muestra el motivo y se ofrece reintentar. */
    data class Error(
        val message: String,
        val onRetry: (() -> Unit)? = null,
    ) : ScreenState
}

/**
 * Contenedor estándar de pantalla.
 *
 * Todas las pantallas deberían pasar por aquí. Da, en este orden:
 * barra superior fija, acción principal opcional, y el contenido con el estado
 * ya resuelto. Evita que cada pantalla repita el `Surface` + `Column` + el
 * `if (loading)` a mano.
 *
 * @param scroll por defecto la pantalla hace scroll; se desactiva en listas
 *   perezosas (`LazyColumn`), que gestionan su propio desplazamiento.
 */
@Composable
fun ITScreenScaffold(
    title: String,
    state: ScreenState = ScreenState.Ready,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit)? = null,
    skeleton: (@Composable () -> Unit)? = null,
    scroll: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier = modifier.fillMaxSize(), color = AxzyColors.background) {
        // El botón de menú se hereda del shell: si hay drawer disponible, se
        // pinta sin que cada pantalla tenga que pedirlo.
        val openDrawer = LocalOnOpenDrawer.current
        // Si la pantalla puede volver, el botón de volver manda sobre el menú:
        // el menú lateral sólo se inyecta en las pantallas raíz (sin back).
        val resolvedNavigationIcon = navigationIcon
            ?: if (onBack == null) openDrawer?.let { open -> { MenuButton(onClick = open) } } else null

        // Safe area: la barra superior ya consume el inset de arriba, así que
        // aquí sólo se reserva el de abajo (barra de gestos o de navegación).
        // Sin esto, el último elemento de una lista queda bajo la barra del
        // sistema y no se puede pulsar.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
        ) {
            ITAppBar(
                title = title,
                subtitle = subtitle,
                onBack = onBack,
                navigationIcon = resolvedNavigationIcon,
                actions = actions,
            )

            Box(modifier = Modifier.weight(1f)) {
                when (state) {
                    ScreenState.Loading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    horizontal = AxzySpacing.screenH,
                                    vertical = AxzySpacing.screenV,
                                ),
                        ) {
                            if (skeleton != null) skeleton() else ITSkeletonList()
                        }
                    }

                    is ScreenState.Empty -> ITEmptyState(
                        title = state.title,
                        description = state.description,
                        actionLabel = state.actionLabel,
                        onAction = state.onAction,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    is ScreenState.Error -> ITEmptyState(
                        title = "No se pudo cargar",
                        description = state.message,
                        actionLabel = if (state.onRetry != null) "Reintentar" else null,
                        onAction = state.onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    ScreenState.Ready -> {
                        val base = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = AxzySpacing.screenH,
                                vertical = AxzySpacing.screenV,
                            )

                        Column(
                            modifier = if (scroll) base.verticalScroll(rememberScrollState()) else base,
                            verticalArrangement = Arrangement.spacedBy(AxzySpacing.cardGap),
                            content = content,
                        )
                    }
                }
            }
        }

        floatingActionButton?.let { fab ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                Box(modifier = Modifier.padding(AxzySpacing.lg)) { fab() }
            }
        }
    }
}
