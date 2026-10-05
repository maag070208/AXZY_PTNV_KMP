package com.axzydev.checkapp.pages.uniformcheck.viewmodel

data class ChecklistItemUi(val key: String, val label: String, val ok: Boolean)

data class Option(val id: String, val label: String)

data class UniformCheckUiState(
    val loading: Boolean = true,
    val guards: List<Option> = emptyList(),
    val selectedGuardId: String? = null,
    val items: List<ChecklistItemUi> = emptyList(),
    val notes: String = "",
    val saving: Boolean = false,
    val catalogMissing: Boolean = false,
    val error: String? = null,
    val savedCount: Int = 0,
) {
    val score: Int get() = if (items.isEmpty()) 0 else items.count { it.ok } * 100 / items.size
}

sealed interface UniformCheckAction {
    data class SelectGuard(val guardId: String) : UniformCheckAction
    data class Toggle(val key: String, val ok: Boolean) : UniformCheckAction
    data class Notes(val value: String) : UniformCheckAction
    data object Save : UniformCheckAction
}

sealed interface UniformCheckEffect {
    data class Error(val message: String) : UniformCheckEffect
    data object Saved : UniformCheckEffect
}
