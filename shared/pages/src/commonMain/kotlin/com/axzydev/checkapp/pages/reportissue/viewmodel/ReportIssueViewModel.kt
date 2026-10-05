package com.axzydev.checkapp.pages.reportissue.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.entities.incident.model.IncidentDraft
import com.axzydev.checkapp.entities.maintenance.model.MaintenanceDraft
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import com.axzydev.checkapp.features.reportissue.model.ListIssueCategoriesUseCase
import com.axzydev.checkapp.features.reportissue.model.ReportIncidentUseCase
import com.axzydev.checkapp.features.reportissue.model.ReportMaintenanceUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Reporte de incidencias/fallas desde el flujo del guardia.
 * Guarda local-first (`_status = created`) y `core:sync` lo sube luego.
 */
class ReportIssueViewModel(
    private val listCategories: ListIssueCategoriesUseCase,
    private val reportIncident: ReportIncidentUseCase,
    private val reportMaintenance: ReportMaintenanceUseCase,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportIssueUiState())
    val state: StateFlow<ReportIssueUiState> = _state.asStateFlow()

    private val _effects = Channel<ReportIssueEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var kind: ReportIssueKind = ReportIssueKind.INCIDENT

    fun load(kind: ReportIssueKind) {
        this.kind = kind
        _state.update { it.copy(kind = kind, loading = true, error = null) }
        viewModelScope.launch {
            runCatching { listCategories(kind.categoryType).map { CategoryOption(it.id, it.name) } }.fold(
                onSuccess = { categories -> _state.update { it.copy(loading = false, categories = categories) } },
                onFailure = { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar las categorías") }
                },
            )
        }
    }

    fun onAction(action: ReportIssueAction) {
        when (action) {
            is ReportIssueAction.Title -> _state.update { it.copy(title = action.value, error = null) }
            is ReportIssueAction.Description -> _state.update { it.copy(description = action.value, error = null) }
            is ReportIssueAction.SelectCategory -> _state.update { it.copy(selectedCategoryId = action.id, error = null) }
            is ReportIssueAction.AddMedia -> _state.update { current ->
                if (current.media.any { it.uri == action.uri }) {
                    current
                } else {
                    current.copy(media = current.media + ReportMedia(action.uri, action.uri, action.isVideo))
                }
            }

            is ReportIssueAction.RemoveMedia -> _state.update { current ->
                current.copy(media = current.media.filterNot { it.id == action.id })
            }

            is ReportIssueAction.MediaError -> _state.update { it.copy(error = action.message) }
            ReportIssueAction.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        if (current.title.trim().isBlank()) {
            _state.update { it.copy(error = "Escribe un título") }
            return
        }
        val session = sessionRepository.sessionState.value
        if (session == null) {
            _state.update { it.copy(error = "Sesión no disponible") }
            return
        }
        val mediaUris = current.media.map { it.uri }
        val title = current.title.trim()
        val description = current.description.trim().ifBlank { null }

        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                when (kind) {
                    ReportIssueKind.INCIDENT -> reportIncident(
                        session.userId,
                        IncidentDraft(
                            title = title,
                            description = description,
                            categoryId = current.selectedCategoryId,
                            clientId = session.clientId,
                            media = mediaUris,
                        ),
                    )

                    ReportIssueKind.MAINTENANCE -> reportMaintenance(
                        session.userId,
                        MaintenanceDraft(
                            title = title,
                            description = description,
                            categoryId = current.selectedCategoryId,
                            clientId = session.clientId,
                            media = mediaUris,
                        ),
                    )
                }
            }.fold(
                onSuccess = {
                    _state.update { it.copy(saving = false) }
                    _effects.send(ReportIssueEffect.Saved)
                },
                onFailure = { error ->
                    val message = error.message ?: "No se pudo guardar el reporte"
                    _state.update { it.copy(saving = false, error = message) }
                    _effects.send(ReportIssueEffect.Error(message))
                },
            )
        }
    }
}
