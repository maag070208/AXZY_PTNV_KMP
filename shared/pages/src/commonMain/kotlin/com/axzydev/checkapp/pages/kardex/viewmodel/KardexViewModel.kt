package com.axzydev.checkapp.pages.kardex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.entities.kardex.data.KardexRepository
import com.axzydev.checkapp.entities.location.data.LocationRepository
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KardexViewModel(
    private val sessionRepository: SessionRepository,
    private val kardex: KardexRepository,
    private val locations: LocationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(KardexUiState())
    val state: StateFlow<KardexUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: KardexAction) {
        when (action) {
            KardexAction.Refresh -> refresh()
            is KardexAction.Search -> _state.update { it.copy(query = action.value) }
        }
    }

    private suspend fun load() {
        val session = sessionRepository.sessionState.value ?: return
        _state.update { it.copy(loading = true, error = null) }
        runCatching {
            val names = locations.activeLocations(session.clientId).associate { it.id to it.name }
            kardex.byUser(session.userId).map { entry ->
                KardexItemUi(
                    id = entry.id,
                    locationName = names[entry.locationId] ?: entry.locationId,
                    timestamp = entry.timestamp,
                    notes = entry.notes,
                    mediaCount = entry.media.size,
                )
            }
        }.fold(
            onSuccess = { items -> _state.update { it.copy(loading = false, items = items) } },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudo cargar el historial") }
            },
        )
    }
}
