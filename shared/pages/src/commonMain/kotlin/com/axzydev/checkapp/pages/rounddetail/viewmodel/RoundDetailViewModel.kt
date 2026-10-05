package com.axzydev.checkapp.pages.rounddetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.entities.kardex.data.KardexRepository
import com.axzydev.checkapp.entities.location.data.LocationRepository
import com.axzydev.checkapp.entities.round.data.RoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoundDetailViewModel(
    private val rounds: RoundRepository,
    private val kardex: KardexRepository,
    private val locations: LocationRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(RoundDetailUiState())
    val state: StateFlow<RoundDetailUiState> = _state.asStateFlow()

    private var roundId: String = ""

    fun load(roundId: String) {
        this.roundId = roundId
        viewModelScope.launch { refreshInternal() }
    }

    fun onAction(action: RoundDetailAction) {
        when (action) {
            RoundDetailAction.Refresh -> viewModelScope.launch { refreshInternal() }
        }
    }

    private suspend fun refreshInternal() {
        _state.update { it.copy(loading = true, error = null) }
        val round = rounds.findById(roundId)
        if (round == null) {
            _state.update { it.copy(loading = false, error = "Ronda no encontrada") }
            return
        }
        val names = locations.activeLocations(round.clientId).associate { it.id to it.name }
        val end = round.endTime ?: timeProvider.nowEpochMillis()
        val marks = kardex.between(round.startTime, end)

        val timeline = buildList {
            add(TimelineItemUi("Inicio de ronda", round.startTime, null))
            marks.forEach { mark ->
                add(
                    TimelineItemUi(
                        label = "Punto: ${names[mark.locationId] ?: mark.locationId}",
                        timestamp = mark.timestamp,
                        detail = mark.notes?.takeIf { it.isNotBlank() },
                    ),
                )
            }
            round.endTime?.let { add(TimelineItemUi("Fin de ronda", it, null)) }
        }

        _state.update {
            it.copy(
                loading = false,
                status = round.status.name,
                startTime = round.startTime,
                endTime = round.endTime,
                timeline = timeline,
            )
        }
    }
}
