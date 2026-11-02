package com.axzydev.checkapp.pages.guarddashboard.viewmodel

data class PointUi(
    val locationId: String,
    val name: String,
    val order: Int,
    val taskCount: Int,
    val verified: Boolean,
)

data class RouteUi(
    val id: String,
    val title: String,
    val pointCount: Int,
)

data class RoundHistoryUi(
    val id: String,
    val startTime: Long,
    val endTime: Long?,
    val status: String,
)

data class GuardDashboardUiState(
    val loading: Boolean = true,
    val userName: String = "",
    val routes: List<RouteUi> = emptyList(),
    val activeRoundId: String? = null,
    val activeRouteId: String? = null,
    val points: List<PointUi> = emptyList(),
    val history: List<RoundHistoryUi> = emptyList(),
    val panic: PanicUi? = null,
    val error: String? = null,
    /** Ruta esperando confirmación: es lo que abre el modal de "¿iniciar?". */
    val pendingStart: RouteUi? = null,
    /** Selector abierto: hay más de una ruta y hay que elegir cuál. */
    val routePickerOpen: Boolean = false,
    /** Se está iniciando: el modal se queda con el botón en carga. */
    val starting: Boolean = false,
    /** El escáner está abierto encima del panel; no es otra pantalla. */
    val scanning: Boolean = false,
    /** Se está resolviendo el código escaneado. */
    val resolvingScan: Boolean = false,
) {
    val hasActiveRound: Boolean get() = activeRoundId != null
}

/** Estado del overlay de pánico. */
data class PanicUi(val title: String, val message: String)

sealed interface GuardDashboardAction {
    data object Refresh : GuardDashboardAction

    /**
     * El bloque del escáner cuando no hay ruta activa.
     *
     * Decide solo: con una ruta la inicia, con varias abre el selector. Sin rutas
     * no hace nada —para eso el bloque ya ofrece sincronizar—.
     */
    data object RequestStart : GuardDashboardAction

    /** Elegir una ruta en el selector: esa es la que se inicia. */
    data class PickRoute(val routeId: String) : GuardDashboardAction

    /** Tocar una ruta de la lista: abre su confirmación. */
    data class RequestStartRoute(val routeId: String) : GuardDashboardAction

    /** Confirmar en el modal: aquí sí se inicia. */
    data object ConfirmStart : GuardDashboardAction
    data object CancelStart : GuardDashboardAction
    data object DismissRoutePicker : GuardDashboardAction

    /** Abrir el escáner encima del panel. */
    data object OpenScanner : GuardDashboardAction
    data object CloseScanner : GuardDashboardAction

    /** Código leído: se busca a qué punto corresponde. */
    data class Scanned(val code: String) : GuardDashboardAction

    data object EndRound : GuardDashboardAction
    data object TriggerPanic : GuardDashboardAction
    data object DismissPanic : GuardDashboardAction
}

sealed interface GuardDashboardEffect {
    data class Error(val message: String) : GuardDashboardEffect
    data object RoundFinished : GuardDashboardEffect

    /** El código corresponde a este punto: se abre su reporte. */
    data class OpenCheck(val locationId: String) : GuardDashboardEffect
}
