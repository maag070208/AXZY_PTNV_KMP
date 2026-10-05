package com.axzydev.checkapp.pages.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.features.dashboard.model.GetDashboardStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * View-model del inicio.
 *
 * Sólo alimenta las tres cifras de la cabecera; la navegación sigue viniendo de
 * fuera como callbacks, porque el inicio es un menú y no tiene lógica propia más
 * allá de esos contadores.
 */
class HomeViewModel(
    private val getDashboardStats: GetDashboardStatsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun refreshStats() {
        _state.update { it.copy(loadingStats = true, statsError = null) }
        viewModelScope.launch {
            when (val result = getDashboardStats()) {
                is ApiResult.Success -> _state.update {
                    it.copy(stats = result.data, loadingStats = false)
                }

                is ApiResult.Failure -> _state.update {
                    // Se conservan las cifras anteriores: es preferible un número
                    // viejo acompañado de un aviso que tres ceros sin explicación.
                    it.copy(loadingStats = false, statsError = result.firstMessage)
                }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.RefreshStats -> refreshStats()
        }
    }
}
