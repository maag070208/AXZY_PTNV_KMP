package com.axzydev.checkapp.pages.checkscan.viewmodel

data class CheckScanUiState(
    val code: String = "",
    val loading: Boolean = false,
    val cameraActive: Boolean = false,
    val error: String? = null,
)

sealed interface CheckScanAction {
    data class Code(val value: String) : CheckScanAction
    data object Submit : CheckScanAction
    data object StartCamera : CheckScanAction
    data object StopCamera : CheckScanAction
    data class CameraScanned(val code: String) : CheckScanAction
}

sealed interface CheckScanEffect {
    data class Found(val locationId: String) : CheckScanEffect
    data class NotFound(val code: String) : CheckScanEffect
}
