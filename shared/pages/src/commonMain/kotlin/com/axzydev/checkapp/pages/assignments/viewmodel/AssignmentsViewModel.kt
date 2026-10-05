package com.axzydev.checkapp.pages.assignments.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.assignment.model.AssignmentDraft
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import com.axzydev.checkapp.features.crudassignment.model.CreateAssignmentUseCase
import com.axzydev.checkapp.features.crudassignment.model.DeleteAssignmentUseCase
import com.axzydev.checkapp.features.crudassignment.model.ListAssignmentsUseCase
import com.axzydev.checkapp.features.crudassignment.model.UpdateAssignmentStatusUseCase
import com.axzydev.checkapp.features.crudlocation.model.ListLocationsUseCase
import com.axzydev.checkapp.features.guardlist.model.ListGuardsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssignmentsViewModel(
    private val listAssignments: ListAssignmentsUseCase,
    private val createAssignment: CreateAssignmentUseCase,
    private val updateAssignmentStatus: UpdateAssignmentStatusUseCase,
    private val deleteAssignment: DeleteAssignmentUseCase,
    private val listGuards: ListGuardsUseCase,
    private val listLocations: ListLocationsUseCase,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AssignmentsUiState())
    val state: StateFlow<AssignmentsUiState> = _state.asStateFlow()

    private val _effects = Channel<AssignmentsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: AssignmentsAction) {
        when (action) {
            AssignmentsAction.Refresh -> refresh()
            is AssignmentsAction.Search -> _state.update { it.copy(query = action.value) }
            AssignmentsAction.ToggleForm -> _state.update { it.copy(showForm = !it.showForm, error = null) }
            is AssignmentsAction.SelectGuard -> _state.update { it.copy(selectedGuardId = action.guardId) }
            is AssignmentsAction.SelectLocation -> _state.update { it.copy(selectedLocationId = action.locationId) }
            is AssignmentsAction.Notes -> _state.update { it.copy(notes = action.value) }
            AssignmentsAction.Create -> create()
            is AssignmentsAction.AdvanceStatus -> advanceStatus(action.id, action.currentStatus)
            is AssignmentsAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteName = current.items.firstOrNull { it.id == action.id }?.guardName,
                )
            }

            AssignmentsAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteName = null) }
            AssignmentsAction.ConfirmDelete -> confirmDelete()
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching {
            val guards = listGuards().map { Option(it.id, it.fullName) }
            val locations = listLocations().map { Option(it.id, it.name) }
            val assignments = listAssignments().map { AssignmentItemUi(it.id, it.guardName, it.locationName, it.status) }
            Triple(guards, locations, assignments)
        }.fold(
            onSuccess = { (guards, locations, assignments) ->
                _state.update { it.copy(loading = false, guards = guards, locations = locations, items = assignments) }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar las asignaciones") }
            },
        )
    }

    private fun create() {
        val current = _state.value
        val guardId = current.selectedGuardId
        val locationId = current.selectedLocationId
        val assignedBy = sessionRepository.sessionState.value?.userId
        if (guardId == null || locationId == null || assignedBy == null) {
            _state.update { it.copy(error = "Selecciona un guardia y una ubicación") }
            return
        }
        _state.update { it.copy(creating = true, error = null) }
        viewModelScope.launch {
            val draft = AssignmentDraft(
                guardId = guardId,
                locationId = locationId,
                assignedBy = assignedBy,
                notes = current.notes.trim().ifBlank { null },
            )
            when (val result = createAssignment(draft)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(creating = false, showForm = false, selectedGuardId = null, selectedLocationId = null, notes = "")
                    }
                    _effects.send(AssignmentsEffect.Created)
                    load()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(creating = false, error = result.firstMessage) }
                    _effects.send(AssignmentsEffect.Error(result.firstMessage))
                }
            }
        }
    }

    private fun advanceStatus(id: String, currentStatus: String) {
        val index = AssignmentStatuses.indexOf(currentStatus)
        val next = AssignmentStatuses[(index + 1).mod(AssignmentStatuses.size)]
        _state.update { it.copy(statusUpdatingId = id, error = null) }
        viewModelScope.launch {
            when (val result = updateAssignmentStatus(id, next)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(statusUpdatingId = null) }
                    _effects.send(AssignmentsEffect.Updated)
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(statusUpdatingId = null, error = result.firstMessage) }
            }
        }
    }

    private fun confirmDelete() {
        val id = _state.value.pendingDeleteId ?: return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteAssignment(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleting = false, pendingDeleteId = null, pendingDeleteName = null) }
                    _effects.send(AssignmentsEffect.Deleted)
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(deleting = false, error = result.firstMessage) }
            }
        }
    }
}
