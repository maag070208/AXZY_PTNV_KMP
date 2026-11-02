package com.axzydev.checkapp.pages.reportissue.viewmodel

import com.axzydev.checkapp.design.components.MediaSyncState
import com.axzydev.checkapp.entities.incidentcategory.model.IncidentCategory

/** Tipo de reporte del guardia; determina el catálogo de categorías. */
enum class ReportIssueKind(val categoryType: String, val label: String) {
    INCIDENT(IncidentCategory.TYPE_INCIDENT, "Reportar incidencia"),
    MAINTENANCE(IncidentCategory.TYPE_MAINTENANCE, "Reportar falla"),
}

/** Sub-tipo de una categoría, con el nombre ya legible. */
data class TypeOption(val id: String, val name: String)

data class CategoryOption(
    val id: String,
    val name: String,
    /** Hex del catálogo ("#EF4444") y nombre del icono ("shield-alert"). */
    val color: String? = null,
    val icon: String? = null,
    val types: List<TypeOption> = emptyList(),
)

/**
 * Evidencia del reporte.
 *
 * [uri] cambia de local a URL pública cuando la subida termina: el reporte viaja con
 * la URL, y lo que no se pudo subir viaja como archivo local para que el sync lo
 * levante después. [state] y [error] son para el guardia, no para el servidor.
 */
data class ReportMedia(
    val id: String,
    val uri: String,
    val isVideo: Boolean,
    val state: MediaSyncState = MediaSyncState.LOCAL,
    val error: String? = null,
)

data class ReportIssueUiState(
    val kind: ReportIssueKind = ReportIssueKind.INCIDENT,
    val loading: Boolean = true,
    val categories: List<CategoryOption> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedTypeId: String? = null,
    val description: String = "",
    val media: List<ReportMedia> = emptyList(),
    /** Evidencia abierta en el visor, si hay alguna. */
    val openMediaId: String? = null,
    val saving: Boolean = false,
    /**
     * Falló la **carga del catálogo**: sin categorías no se puede reportar nada, así
     * que esto pinta el estado de error de la pantalla, con reintento.
     */
    val loadError: String? = null,
    /**
     * Falló el **formulario** (validación, cámara o guardado): se corrige sin salir,
     * así que va en una banda dentro de la pantalla.
     */
    val error: String? = null,
) {
    /** Categoría elegida, con sus sub-tipos. */
    val selectedCategory: CategoryOption?
        get() = categories.firstOrNull { it.id == selectedCategoryId }

    /** Sub-tipos de la categoría elegida; vacío si no hay ninguna elegida. */
    val typeOptions: List<TypeOption>
        get() = selectedCategory?.types.orEmpty()

    /**
     * Título del reporte: **no se escribe, se deduce**.
     *
     * "SEGURIDAD · Robo / Hurto" dice más que "Falló algo" y no cuesta un campo
     * más en el formulario. Si la categoría no tiene sub-tipos queda sólo su
     * nombre; mientras no haya categoría no hay título y el botón no se habilita.
     */
    val title: String
        get() {
            val category = selectedCategory ?: return ""
            val type = typeOptions.firstOrNull { it.id == selectedTypeId }
            return listOfNotNull(category.name, type?.name).joinToString(" · ")
        }

    /** Descripción válida: es la parte que explica qué pasó. */
    val descriptionMissing: Int
        get() = (MIN_DESCRIPTION_LENGTH - description.trim().length).coerceAtLeast(0)

    val descriptionValid: Boolean get() = descriptionMissing == 0

    /**
     * Qué falta para poder enviar, **en palabras**.
     *
     * El botón deshabilitado no explica nada: quien no sabe qué le falta toca el botón
     * y no pasa nada. La pantalla muestra esta lista tal cual.
     */
    val missingRequirements: List<String>
        get() = buildList {
            if (selectedCategory == null) add("Categoría")
            if (typeOptions.isNotEmpty() && selectedTypeId == null) add("Tipo")
            if (!descriptionValid) add("Descripción (mínimo $MIN_DESCRIPTION_LENGTH caracteres)")
        }

    /**
     * El botón sólo se habilita cuando hay lo mínimo para enviar: categoría y
     * —si esa categoría tiene sub-tipos— el sub-tipo (el título se deduce de ambos).
     * Se exige el sub-tipo porque es lo que hace útil el reporte; sin él, "SEGURIDAD"
     * no dice qué pasó. Si la categoría no tiene sub-tipos no se pide, que no se puede
     * pedir lo que no hay.
     */
    val canSubmit: Boolean
        get() = !saving &&
            selectedCategory != null &&
            (typeOptions.isEmpty() || selectedTypeId != null) &&
            descriptionValid
}

/**
 * Mínimo de la descripción.
 *
 * El reporte lo lee alguien que no estaba ahí: con "falla" o "no sirve" no puede hacer
 * nada. Veinte caracteres no garantizan una buena descripción, pero obligan a escribir
 * una frase.
 */
const val MIN_DESCRIPTION_LENGTH = 20

sealed interface ReportIssueAction {
    data class Description(val value: String) : ReportIssueAction
    data class SelectCategory(val id: String) : ReportIssueAction
    data class SelectType(val id: String) : ReportIssueAction
    data class AddMedia(val uri: String, val isVideo: Boolean) : ReportIssueAction
    data class RemoveMedia(val id: String) : ReportIssueAction

    /** Reintentar la subida de una evidencia que falló. */
    data class RetryMedia(val id: String) : ReportIssueAction

    data class OpenMedia(val id: String) : ReportIssueAction
    data object CloseMedia : ReportIssueAction
    data class MediaError(val message: String) : ReportIssueAction
    data object Retry : ReportIssueAction
    data object Submit : ReportIssueAction
}

sealed interface ReportIssueEffect {
    data object Saved : ReportIssueEffect
    data class Error(val message: String) : ReportIssueEffect
}
