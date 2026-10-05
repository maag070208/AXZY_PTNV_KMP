package com.axzydev.checkapp.pages.sync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.sync.SyncStep
import com.axzydev.checkapp.features.syncdatabase.model.SyncDatabaseUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SyncViewModel(
    private val sync: SyncDatabaseUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SyncUiState())
    val state: StateFlow<SyncUiState> = _state.asStateFlow()

    private val _effects = Channel<SyncEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var started = false

    fun start() {
        if (started) return
        started = true
        run()
    }

    fun onAction(action: SyncAction) {
        when (action) {
            SyncAction.Retry -> run()
        }
    }

    private fun run() {
        _state.value = SyncUiState(status = SyncRunStatus.SYNCING)
        viewModelScope.launch {
            updateStep(1, SyncStepStatus.ACTIVE)
            val hasPending = runCatching { sync.hasPendingServerChanges() }.getOrDefault(false)
            _state.update {
                it.copy(
                    serverStatus = if (hasPending) ServerStatus.UPDATES_PENDING else ServerStatus.UP_TO_DATE,
                )
            }
            updateStep(1, SyncStepStatus.SUCCESS)

            sync.run { step -> handleStep(step) }.fold(
                onSuccess = {
                    _state.update { current ->
                        current.copy(
                            status = SyncRunStatus.SUCCESS,
                            upload = null,
                            steps = current.steps.map { it.copy(status = SyncStepStatus.SUCCESS) },
                        )
                    }
                    _effects.send(SyncEffect.Completed)
                },
                onFailure = { throwable ->
                    failActiveStep()
                    _state.update {
                        it.copy(
                            status = SyncRunStatus.ERROR,
                            error = throwable.message ?: "Error al sincronizar",
                        )
                    }
                },
            )
        }
    }

    private fun handleStep(step: SyncStep) {
        when (step) {
            SyncStep.Pull -> updateStep(2, SyncStepStatus.ACTIVE)
            is SyncStep.PullDataReceived -> updateStep(2, SyncStepStatus.ACTIVE)
            SyncStep.Push -> {
                updateStep(2, SyncStepStatus.SUCCESS)
                updateStep(3, SyncStepStatus.ACTIVE)
            }

            is SyncStep.MediaUploadStart -> {
                updateStep(2, SyncStepStatus.SUCCESS)
                updateStep(3, SyncStepStatus.ACTIVE)
                _state.update { it.copy(upload = UploadProgress(0, step.total)) }
            }

            is SyncStep.MediaUploadProgress -> {
                updateStep(2, SyncStepStatus.SUCCESS)
                updateStep(3, SyncStepStatus.ACTIVE)
                _state.update { it.copy(upload = UploadProgress(step.current, step.total)) }
            }
        }
    }

    private fun updateStep(id: Int, status: SyncStepStatus) {
        _state.update { current ->
            current.copy(steps = current.steps.map { if (it.id == id) it.copy(status = status) else it })
        }
    }

    private fun failActiveStep() {
        _state.update { current ->
            current.copy(
                steps = current.steps.map {
                    if (it.status == SyncStepStatus.ACTIVE) it.copy(status = SyncStepStatus.ERROR) else it
                },
            )
        }
    }
}
