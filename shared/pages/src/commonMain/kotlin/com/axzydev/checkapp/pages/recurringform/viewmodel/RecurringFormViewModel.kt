package com.axzydev.checkapp.pages.recurringform.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.recurringroute.model.RecurringPointDraft
import com.axzydev.checkapp.entities.recurringroute.model.RecurringRouteDraft
import com.axzydev.checkapp.entities.recurringroute.model.RecurringTaskDraft
import com.axzydev.checkapp.features.crudclient.model.ListClientsUseCase
import com.axzydev.checkapp.features.crudlocation.model.ListLocationsUseCase
import com.axzydev.checkapp.features.crudrecurring.model.CreateRecurringUseCase
import com.axzydev.checkapp.features.crudrecurring.model.GetRecurringUseCase
import com.axzydev.checkapp.features.crudrecurring.model.UpdateRecurringUseCase
import com.axzydev.checkapp.features.crudzone.model.ListZonesUseCase
import com.axzydev.checkapp.features.guardlist.model.ListGuardsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Wizard de rutas recurrentes (4 pasos: Info → Recorrido → Asignación → Resumen).
 * Alta y edición contra REST (`/recurring`); los catálogos caen al caché local si
 * no hay red.
 */
class RecurringFormViewModel(
    private val listClients: ListClientsUseCase,
    private val listZones: ListZonesUseCase,
    private val listLocations: ListLocationsUseCase,
    private val listGuards: ListGuardsUseCase,
    private val getRecurring: GetRecurringUseCase,
    private val createRecurring: CreateRecurringUseCase,
    private val updateRecurring: UpdateRecurringUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RecurringFormUiState())
    val state: StateFlow<RecurringFormUiState> = _state.asStateFlow()

    private val _effects = Channel<RecurringFormEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var routeId: String? = null

    fun load(id: String?) {
        routeId = id
        viewModelScope.launch {
            _state.update { it.copy(loading = true, isEditing = id != null, error = null) }
            runCatching {
                val clients = listClients().map { ClientOption(it.id, it.name) }
                val locations = listLocations().map { LocationOption(it.id, it.name, it.zoneId, it.clientId) }
                val guards = listGuards().filter { it.active }.map { GuardOption(it.id, it.fullName, it.clientId) }
                Triple(clients, locations, guards)
            }.fold(
                onSuccess = { (clients, locations, guards) ->
                    _state.update { it.copy(clients = clients, locations = locations, guards = guards) }
                    if (id != null) prefill(id) else _state.update { it.copy(loading = false) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(loading = false, error = error.message ?: "No se pudieron cargar los catálogos")
                    }
                },
            )
        }
    }

    private suspend fun prefill(id: String) {
        when (val result = getRecurring(id)) {
            is ApiResult.Success -> {
                val route = result.data
                _state.update { current ->
                    current.copy(
                        loading = false,
                        title = route.title,
                        selectedClientId = route.clientId,
                        points = route.points.map { point ->
                            PointUi(
                                locationId = point.locationId,
                                locationName = point.locationName ?: point.locationId,
                                tasks = point.tasks.map { TaskUi(it.description, it.reqPhoto) },
                            )
                        },
                        selectedGuardIds = route.guardIds.toSet(),
                    )
                }
                route.clientId?.let { loadZones(it) }
            }

            is ApiResult.Failure ->
                _state.update { it.copy(loading = false, error = result.messages.firstOrNull() ?: "No se pudo cargar la ruta") }
        }
    }

    private suspend fun loadZones(clientId: String) {
        val zones = runCatching { listZones(clientId).map { ZoneOption(it.id, it.name) } }.getOrDefault(emptyList())
        _state.update { it.copy(zones = zones) }
    }

    fun onAction(action: RecurringFormAction) {
        when (action) {
            is RecurringFormAction.Title -> _state.update { it.copy(title = action.value, error = null) }
            is RecurringFormAction.SelectClient -> selectClient(action.clientId)
            is RecurringFormAction.SelectZone -> _state.update { it.copy(selectedZoneId = action.zoneId) }
            is RecurringFormAction.AddLocation -> addLocation(action.locationId)
            RecurringFormAction.AddAllFromZone -> addAllFromZone()
            is RecurringFormAction.RemovePoint -> _state.update { current ->
                current.copy(points = current.points.filterIndexed { index, _ -> index != action.index })
            }

            is RecurringFormAction.AddTask -> updatePoint(action.pointIndex) { point ->
                point.copy(tasks = point.tasks + TaskUi("", false))
            }

            is RecurringFormAction.TaskDescription -> updatePoint(action.pointIndex) { point ->
                point.copy(tasks = point.tasks.mapIndexed { index, task ->
                    if (index == action.taskIndex) task.copy(description = action.value) else task
                })
            }

            is RecurringFormAction.ToggleTaskPhoto -> updatePoint(action.pointIndex) { point ->
                point.copy(tasks = point.tasks.mapIndexed { index, task ->
                    if (index == action.taskIndex) task.copy(reqPhoto = !task.reqPhoto) else task
                })
            }

            is RecurringFormAction.RemoveTask -> updatePoint(action.pointIndex) { point ->
                point.copy(tasks = point.tasks.filterIndexed { index, _ -> index != action.taskIndex })
            }

            is RecurringFormAction.ToggleGuard -> _state.update { current ->
                val guards = if (action.guardId in current.selectedGuardIds) {
                    current.selectedGuardIds - action.guardId
                } else {
                    current.selectedGuardIds + action.guardId
                }
                current.copy(selectedGuardIds = guards, error = null)
            }

            RecurringFormAction.SelectAllGuards -> _state.update { current ->
                current.copy(selectedGuardIds = current.availableGuards.map { it.id }.toSet())
            }

            RecurringFormAction.ClearGuards -> _state.update { it.copy(selectedGuardIds = emptySet()) }
            RecurringFormAction.Next -> next()
            RecurringFormAction.Back -> _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0), error = null) }
            RecurringFormAction.Submit -> submit()
            RecurringFormAction.DismissError -> _state.update { it.copy(error = null) }
        }
    }

    private fun selectClient(clientId: String) {
        _state.update {
            it.copy(
                selectedClientId = clientId,
                selectedZoneId = null,
                points = emptyList(),
                selectedGuardIds = emptySet(),
                error = null,
            )
        }
        viewModelScope.launch { loadZones(clientId) }
    }

    private fun addLocation(locationId: String) {
        _state.update { current ->
            if (current.points.any { it.locationId == locationId }) return@update current
            val location = current.locations.firstOrNull { it.id == locationId } ?: return@update current
            current.copy(points = current.points + PointUi(location.id, location.name, emptyList()))
        }
    }

    private fun addAllFromZone() {
        _state.update { current ->
            val zoneId = current.selectedZoneId ?: return@update current
            val toAdd = current.locations
                .filter { it.zoneId == zoneId && it.clientId == current.selectedClientId }
                .filter { location -> current.points.none { it.locationId == location.id } }
                .map { PointUi(it.id, it.name, emptyList()) }
            current.copy(points = current.points + toAdd, selectedZoneId = null)
        }
    }

    private inline fun updatePoint(index: Int, transform: (PointUi) -> PointUi) {
        _state.update { current ->
            current.copy(points = current.points.mapIndexed { i, point -> if (i == index) transform(point) else point })
        }
    }

    private fun next() {
        val current = _state.value
        val error = when (current.step) {
            0 -> when {
                current.title.isBlank() -> "Escribe el nombre de la ruta"
                current.selectedClientId == null -> "Selecciona un cliente"
                else -> null
            }

            1 -> if (current.points.isEmpty()) "Agrega al menos una ubicación" else null
            2 -> if (current.selectedGuardIds.isEmpty()) "Asigna al menos un guardia" else null
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }
        if (current.step < RecurringFormStepTitles.lastIndex) {
            _state.update { it.copy(step = it.step + 1, error = null) }
        }
    }

    private fun submit() {
        val current = _state.value
        val clientId = current.selectedClientId
        if (clientId == null) {
            _state.update { it.copy(error = "Selecciona un cliente") }
            return
        }
        val draft = RecurringRouteDraft(
            title = current.title.trim(),
            clientId = clientId,
            points = current.points.map { point ->
                RecurringPointDraft(
                    locationId = point.locationId,
                    tasks = point.tasks
                        .filter { it.description.isNotBlank() }
                        .map { RecurringTaskDraft(it.description.trim(), it.reqPhoto) },
                )
            },
            guardIds = current.selectedGuardIds.toList(),
            active = true,
        )

        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val result = runCatching {
                val existing = routeId
                if (existing == null) createRecurring(draft) else updateRecurring(existing, draft)
            }.getOrElse { ApiResult.Failure(listOf(it.message ?: "No se pudo guardar la ruta")) }

            when (result) {
                is ApiResult.Success -> {
                    _state.update { it.copy(saving = false) }
                    _effects.send(RecurringFormEffect.Saved)
                }

                is ApiResult.Failure -> {
                    val message = result.messages.firstOrNull() ?: "No se pudo guardar la ruta"
                    _state.update { it.copy(saving = false, error = message) }
                    _effects.send(RecurringFormEffect.Error(message))
                }
            }
        }
    }
}
