package com.axzydev.checkapp.pages.checkreport.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.entities.location.data.LocationRepository
import com.axzydev.checkapp.entities.recurringroute.data.RecurringRouteRepository
import com.axzydev.checkapp.entities.round.data.RoundRepository
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import com.axzydev.checkapp.features.submitcheck.model.SubmitCheckUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckReportViewModel(
    private val sessionRepository: SessionRepository,
    private val locations: LocationRepository,
    private val recurringRoutes: RecurringRouteRepository,
    private val rounds: RoundRepository,
    private val submitCheck: SubmitCheckUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CheckReportUiState())
    val state: StateFlow<CheckReportUiState> = _state.asStateFlow()

    private val _effects = Channel<CheckReportEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var roundId: String = ""
    private var locationId: String = ""

    fun load(roundId: String, locationId: String) {
        this.roundId = roundId
        this.locationId = locationId
        viewModelScope.launch {
            val session = sessionRepository.sessionState.value
            val location = locations.findById(locationId)
            val round = rounds.findById(roundId)
            val route = session?.let { recurringRoutes.activeRoutes(it.clientId) }
                ?.firstOrNull { it.id == round?.recurringConfigurationId }
            val point = route?.points?.firstOrNull { it.locationId == locationId }

            _state.update {
                it.copy(
                    loading = false,
                    locationName = location?.name ?: locationId,
                    tasks = point?.tasks?.map { task -> CheckTask(task.id, task.description, false) }.orEmpty(),
                )
            }
        }
    }

    fun onAction(action: CheckReportAction) {
        when (action) {
            is CheckReportAction.ToggleTask -> _state.update { current ->
                current.copy(tasks = current.tasks.map { if (it.id == action.id) it.copy(done = !it.done) else it })
            }

            is CheckReportAction.Notes -> _state.update { it.copy(notes = action.value, error = null) }
            is CheckReportAction.AddMedia -> _state.update { current ->
                if (current.media.any { it.uri == action.uri }) {
                    current
                } else {
                    current.copy(
                        media = current.media + EvidenceItem(action.uri, action.uri, action.isVideo),
                        error = null,
                    )
                }
            }

            is CheckReportAction.RemoveMedia -> _state.update { current ->
                current.copy(media = current.media.filterNot { it.id == action.id })
            }

            is CheckReportAction.MediaError -> _state.update { it.copy(error = action.message) }
            CheckReportAction.Submit -> submit()
        }
    }

    private fun submit() {
        val session = sessionRepository.sessionState.value ?: return
        val current = _state.value
        _state.update { it.copy(submitting = true, error = null) }

        viewModelScope.launch {
            val checklist = current.tasks
                .takeIf { it.isNotEmpty() }
                ?.joinToString(separator = "\n") { "[${if (it.done) "x" else " "}] ${it.description}" }
            val finalNotes = listOfNotNull(
                current.notes.trim().ifBlank { null },
                checklist?.let { "--- CHECKLIST ---\n$it" },
            ).joinToString("\n\n").ifBlank { "Check completado" }

            runCatching {
                submitCheck(
                    userId = session.userId,
                    locationId = locationId,
                    notes = finalNotes,
                    media = current.media.map { it.uri },
                    latitude = null,
                    longitude = null,
                    assignmentId = null,
                )
            }.fold(
                onSuccess = {
                    _state.update { it.copy(submitting = false) }
                    _effects.send(CheckReportEffect.Submitted)
                },
                onFailure = { throwable ->
                    _state.update { it.copy(submitting = false, error = throwable.message ?: "No se pudo guardar") }
                    _effects.send(CheckReportEffect.Error(throwable.message ?: "No se pudo guardar"))
                },
            )
        }
    }
}
