package com.axzydev.checkapp.pages.guarddashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.entities.kardex.data.KardexRepository
import com.axzydev.checkapp.entities.panic.model.PanicAlertDraft
import com.axzydev.checkapp.entities.panic.model.PanicResult
import com.axzydev.checkapp.entities.recurringroute.data.RecurringRouteRepository
import com.axzydev.checkapp.entities.round.data.RoundRepository
import com.axzydev.checkapp.entities.round.model.RoundStatus
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import com.axzydev.checkapp.features.panic.model.FlushPanicQueueUseCase
import com.axzydev.checkapp.features.panic.model.TriggerPanicUseCase
import com.axzydev.checkapp.features.roundcontrol.model.EndRoundUseCase
import com.axzydev.checkapp.features.roundcontrol.model.StartRoundUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuardDashboardViewModel(
    private val sessionRepository: SessionRepository,
    private val recurringRoutes: RecurringRouteRepository,
    private val rounds: RoundRepository,
    private val kardex: KardexRepository,
    private val startRound: StartRoundUseCase,
    private val endRound: EndRoundUseCase,
    private val triggerPanicAlert: TriggerPanicUseCase,
    private val flushPanicQueue: FlushPanicQueueUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GuardDashboardUiState())
    val state: StateFlow<GuardDashboardUiState> = _state.asStateFlow()

    private val _effects = Channel<GuardDashboardEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: GuardDashboardAction) {
        when (action) {
            GuardDashboardAction.Refresh -> refresh()
            is GuardDashboardAction.StartRoute -> start(action.routeId)
            GuardDashboardAction.EndRound -> end()
            GuardDashboardAction.TriggerPanic -> panic()
            GuardDashboardAction.DismissPanic -> _state.update { it.copy(panic = null) }
        }
    }

    private suspend fun load() {
        // Reintenta alertas de pánico encoladas sin conexión (best-effort).
        runCatching { flushPanicQueue() }
        val session = sessionRepository.sessionState.value ?: return
        _state.update { it.copy(loading = true, error = null, userName = session.fullName) }

        val routes = recurringRoutes.activeRoutes(session.clientId)
        val round = rounds.inProgressByGuard(session.userId)

        val points = if (round == null) {
            emptyList()
        } else {
            val route = routes.firstOrNull { it.id == round.recurringConfigurationId }
            val marks = kardex.byUser(session.userId).filter { it.timestamp >= round.startTime }
            route?.points?.map { point ->
                val verified = marks.any {
                    it.locationId == point.locationId && (it.media.isNotEmpty() || !it.notes.isNullOrBlank())
                }
                PointUi(
                    locationId = point.locationId,
                    name = point.locationName ?: point.locationId,
                    order = point.order,
                    taskCount = point.tasks.size,
                    verified = verified,
                )
            }.orEmpty()
        }

        _state.update {
            it.copy(
                loading = false,
                routes = routes.map { route -> RouteUi(route.id, route.title, route.points.size) },
                activeRoundId = round?.id,
                activeRouteId = round?.recurringConfigurationId,
                points = points,
                history = rounds.all()
                    .filter { past -> past.status != RoundStatus.IN_PROGRESS }
                    .take(20)
                    .map { past -> RoundHistoryUi(past.id, past.startTime, past.endTime, past.status.name) },
            )
        }
    }

    private fun start(routeId: String) {
        viewModelScope.launch {
            val session = sessionRepository.sessionState.value ?: return@launch
            runCatching { startRound(session.userId, session.clientId, routeId) }
                .onFailure { error -> _effects.send(GuardDashboardEffect.Error(error.message ?: "No se pudo iniciar la ruta")) }
            load()
        }
    }

    private fun end() {
        viewModelScope.launch {
            val roundId = _state.value.activeRoundId ?: return@launch
            runCatching { endRound(roundId) }
                .onFailure { error -> _effects.send(GuardDashboardEffect.Error(error.message ?: "No se pudo finalizar la ruta")) }
            load()
            _effects.send(GuardDashboardEffect.RoundFinished)
        }
    }

    private fun panic() {
        viewModelScope.launch {
            val result = runCatching { triggerPanicAlert(PanicAlertDraft(source = "in_app")) }
                .getOrElse { PanicResult.Failed(it.message ?: "No se pudo enviar la alerta") }
            val panicUi = when (result) {
                PanicResult.Sent -> PanicUi("¡Alerta enviada!", "Tu central fue notificada. Mantén la calma y resguárdate.")
                PanicResult.Queued -> PanicUi("Alerta guardada", "Sin conexión. La alerta se enviará automáticamente al reconectar.")
                is PanicResult.Failed -> PanicUi("No se pudo enviar", result.message)
            }
            _state.update { it.copy(panic = panicUi) }
        }
    }
}
