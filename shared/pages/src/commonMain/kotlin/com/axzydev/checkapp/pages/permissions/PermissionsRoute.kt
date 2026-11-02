package com.axzydev.checkapp.pages.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.axzydev.checkapp.pages.permissions.ui.PermissionsScreen
import androidx.compose.runtime.rememberCoroutineScope
import com.axzydev.checkapp.core.permissions.AppPermission
import com.axzydev.checkapp.core.permissions.PermissionStatus
import com.axzydev.checkapp.platform.permissions.rememberPermissionChecker
import kotlinx.coroutines.launch

/**
 * Permisos del sistema.
 *
 * Sin view-model ni módulo de DI **a propósito**: el estado es el del sistema
 * operativo. Lo único que hace esta ruta es releerlo —al entrar, al volver del
 * diálogo del sistema y al volver de los ajustes— y pasarlo a la pantalla. Un
 * view-model aquí sería una copia que se queda vieja en cuanto el usuario toca algo
 * fuera de la app.
 */
@Composable
fun PermissionsRoute(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onContinue: (() -> Unit)? = null,
) {
    val checker = rememberPermissionChecker()
    val scope = rememberCoroutineScope()
    var statuses by remember { mutableStateOf(emptyMap<AppPermission, PermissionStatus>()) }

    // Consultar y pedir son `suspend`: la respuesta del sistema llega cuando el
    // usuario decide, no en el momento de la llamada.
    suspend fun refresh() {
        statuses = AppPermission.entries.associateWith { checker.status(it) }
    }

    LaunchedEffect(checker) { refresh() }

    // Volver de los ajustes del sistema no avisa de ninguna otra forma: la actividad
    // se reanuda y hay que releer. Es el caso del permiso bloqueado, que sólo se
    // arregla por ahí.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, checker) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) scope.launch { refresh() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PermissionsScreen(
        statuses = statuses,
        onRequest = { permission ->
            scope.launch { statuses = statuses + (permission to checker.request(permission)) }
        },
        onOpenSettings = checker::openSettings,
        onBack = onBack,
        modifier = modifier,
        onContinue = onContinue,
    )
}
