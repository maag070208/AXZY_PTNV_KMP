package com.axzydev.checkapp.pages.reportissue.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.sync.MediaType
import com.axzydev.checkapp.core.sync.MediaUploader
import com.axzydev.checkapp.design.components.MediaSyncState
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
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reporte de incidencias/fallas desde el flujo del guardia.
 * Guarda local-first (`_status = created`) y `core:sync` lo sube luego.
 */
class ReportIssueViewModel(
    private val listCategories: ListIssueCategoriesUseCase,
    private val reportIncident: ReportIncidentUseCase,
    private val reportMaintenance: ReportMaintenanceUseCase,
    private val sessionRepository: SessionRepository,
    private val mediaUploader: MediaUploader,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportIssueUiState())
    val state: StateFlow<ReportIssueUiState> = _state.asStateFlow()

    private val _effects = Channel<ReportIssueEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var kind: ReportIssueKind = ReportIssueKind.INCIDENT

    fun load(kind: ReportIssueKind) {
        this.kind = kind
        _state.update { it.copy(kind = kind, loading = true, loadError = null, error = null) }
        viewModelScope.launch {
            runCatching {
                listCategories(kind.categoryType).map { category ->
                    CategoryOption(
                        id = category.id,
                        name = category.name,
                        color = category.color,
                        icon = category.icon,
                        types = category.types.map { TypeOption(it.id, it.name) },
                    )
                }
            }.fold(
                onSuccess = { categories -> _state.update { it.copy(loading = false, categories = categories) } },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            loadError = error.message ?: "No se pudieron cargar las categorías",
                        )
                    }
                },
            )
        }
    }

    fun onAction(action: ReportIssueAction) {
        when (action) {
            is ReportIssueAction.Description -> _state.update { it.copy(description = action.value, error = null) }
            is ReportIssueAction.SelectCategory -> _state.update { current ->
                // Cambiar de categoría invalida el sub-tipo: era de la anterior, y
                // dejarlo puesto guardaría un reporte con un tipo que no le toca.
                val typeBelongsToCategory = current.categories
                    .firstOrNull { it.id == action.id }
                    ?.types
                    ?.any { it.id == current.selectedTypeId } == true

                current.copy(
                    selectedCategoryId = action.id,
                    selectedTypeId = current.selectedTypeId.takeIf { typeBelongsToCategory },
                    error = null,
                )
            }

            is ReportIssueAction.SelectType -> _state.update { it.copy(selectedTypeId = action.id, error = null) }
            is ReportIssueAction.RetryMedia -> retryMedia(action.id)
            is ReportIssueAction.OpenMedia -> _state.update { it.copy(openMediaId = action.id) }
            ReportIssueAction.CloseMedia -> _state.update { it.copy(openMediaId = null) }
            is ReportIssueAction.AddMedia -> {
                val already = _state.value.media.any { it.uri == action.uri }
                if (!already) {
                    _state.update { current ->
                        current.copy(
                            media = current.media + ReportMedia(
                                id = action.uri,
                                uri = action.uri,
                                isVideo = action.isVideo,
                                state = MediaSyncState.UPLOADING,
                            ),
                        )
                    }
                    // Se sube al capturar y no al enviar: si falla, el guardia todavía
                    // está delante del problema y puede reintentar o repetir la foto.
                    upload(action.uri, action.isVideo)
                }
            }

            is ReportIssueAction.RemoveMedia -> _state.update { current ->
                current.copy(media = current.media.filterNot { it.id == action.id })
            }

            is ReportIssueAction.MediaError -> _state.update { it.copy(error = action.message) }
            ReportIssueAction.Retry -> load(kind)
            ReportIssueAction.Submit -> submit()
        }
    }

    /**
     * Sube una evidencia y deja constancia de cómo fue.
     *
     * **Sin conexión no es un fallo**: la evidencia se queda local, el reporte se
     * guarda igual y el sync la sube cuando vuelva la red. Confundir eso con un error
     * haría que el guardia reintentara algo que no está roto.
     */
    private fun upload(uri: String, isVideo: Boolean) {
        viewModelScope.launch {
            val table = if (_state.value.kind == ReportIssueKind.INCIDENT) "incidents" else "maintenances"
            // Con tope: el subidor reintenta por su cuenta, y si la red se queda
            // colgada el ítem no puede quedarse en "Subiendo" para siempre. Un estado
            // que no termina nunca es peor que un fallo con reintento.
            val result = runCatching {
                withTimeoutOrNull(UPLOAD_BUDGET_MS) {
                    mediaUploader.upload(
                        uri = uri,
                        type = if (isVideo) MediaType.VIDEO else MediaType.IMAGE,
                        location = table,
                        roundId = null,
                    )
                }
            }.getOrNull()

            val url = result?.url
            val state = when {
                result == null || result.networkError -> MediaSyncState.LOCAL
                result.success && url != null -> MediaSyncState.UPLOADED
                else -> MediaSyncState.FAILED
            }
            val error = when (state) {
                MediaSyncState.LOCAL -> "Sin conexión: se enviará con el próximo sync."
                MediaSyncState.FAILED -> result?.error ?: "No se pudo subir la evidencia"
                else -> null
            }

            _state.update { current ->
                current.copy(
                    media = current.media.map { media ->
                        if (media.id != uri) {
                            media
                        } else {
                            media.copy(uri = url ?: media.uri, state = state, error = error)
                        }
                    },
                )
            }
        }
    }

    private fun retryMedia(id: String) {
        val media = _state.value.media.firstOrNull { it.id == id } ?: return
        _state.update { current ->
            current.copy(
                media = current.media.map {
                    if (it.id == id) it.copy(state = MediaSyncState.UPLOADING, error = null) else it
                },
            )
        }
        upload(media.uri, media.isVideo)
    }

    private fun submit() {
        val current = _state.value
        // El botón ya está deshabilitado sin categoría; se comprueba igual porque el
        // estado es la única fuente de verdad de lo que se va a guardar.
        if (current.selectedCategoryId == null) {
            _state.update { it.copy(error = "Elige una categoría") }
            return
        }
        if (current.typeOptions.isNotEmpty() && current.selectedTypeId == null) {
            _state.update { it.copy(error = "Elige de qué tipo") }
            return
        }
        val session = sessionRepository.sessionState.value
        if (session == null) {
            _state.update { it.copy(error = "Sesión no disponible") }
            return
        }
        val mediaUris = current.media.map { it.uri }
        // El título no se pide ni se escribe: se deduce de la categoría y el tipo.
        val title = current.title
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
                            typeId = current.selectedTypeId,
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
                            typeId = current.selectedTypeId,
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

/** Presupuesto total de una subida, reintentos incluidos. */
private const val UPLOAD_BUDGET_MS = 90_000L
