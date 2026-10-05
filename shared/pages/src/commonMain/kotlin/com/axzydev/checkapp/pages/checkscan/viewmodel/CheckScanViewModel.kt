package com.axzydev.checkapp.pages.checkscan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import com.axzydev.checkapp.features.scanqr.model.MatchLocationUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckScanViewModel(
    private val matchLocation: MatchLocationUseCase,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CheckScanUiState())
    val state: StateFlow<CheckScanUiState> = _state.asStateFlow()

    private val _effects = Channel<CheckScanEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /** Último código procesado desde la cámara; evita reenvíos del mismo frame. */
    private var lastScanned: String? = null

    fun onAction(action: CheckScanAction) {
        when (action) {
            is CheckScanAction.Code -> _state.update { it.copy(code = action.value, error = null) }
            CheckScanAction.Submit -> submit()
            CheckScanAction.StartCamera -> {
                lastScanned = null
                _state.update { it.copy(cameraActive = true, error = null) }
            }

            CheckScanAction.StopCamera -> _state.update { it.copy(cameraActive = false) }
            is CheckScanAction.CameraScanned -> onCameraScanned(action.code)
        }
    }

    private fun onCameraScanned(code: String) {
        val trimmed = code.trim()
        if (trimmed.isEmpty() || trimmed == lastScanned || _state.value.loading) return
        lastScanned = trimmed
        _state.update { it.copy(code = trimmed, cameraActive = false, error = null) }
        submit()
    }

    private fun submit() {
        val code = _state.value.code.trim()
        if (code.isEmpty()) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val clientId = sessionRepository.sessionState.value?.clientId
            val result = matchLocation(code, clientId)
            _state.update { it.copy(loading = false) }
            val location = result.location
            if (location != null) {
                _effects.send(CheckScanEffect.Found(location.id))
            } else {
                _state.update { it.copy(error = "Punto no encontrado para el código: $code") }
                _effects.send(CheckScanEffect.NotFound(code))
            }
        }
    }
}
