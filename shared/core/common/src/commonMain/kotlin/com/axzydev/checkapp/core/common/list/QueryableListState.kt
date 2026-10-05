package com.axzydev.checkapp.core.common.list

/**
 * Estado de una lista con búsqueda.
 *
 * Existe para que los doce módulos del inicio no reimplementen lo mismo. Clientes
 * lo hizo a mano primero (`query` + `visibleClients` derivada) y copiar esa receta
 * once veces es garantía de que alguna se desincronice.
 *
 * `visibleItems` es **derivada**, no un campo: si se guardara una lista ya filtrada,
 * al crear o borrar un registro habría que acordarse de recalcularla, y ese olvido
 * es el bug clásico de este patrón.
 */
interface QueryableListState<T> {
    /** Texto del buscador. */
    val query: String

    /** Todos los registros cargados. */
    val items: List<T>

    /** Campos por los que busca el filtro de este módulo. */
    val searchFields: List<(T) -> String?>

    /** Lo que se pinta con el filtro actual. */
    val visibleItems: List<T>
        get() = items.filterBy(query, searchFields)

    /** `true` si hay una búsqueda activa. */
    val isSearching: Boolean get() = query.isNotBlank()
}

/**
 * Filtra por varios campos a la vez, sin distinguir mayúsculas ni acentos.
 *
 * Se compara `ignoreCase` y se ignoran los campos nulos o en blanco, para que un
 * registro sin RFC no desaparezca al buscar por RFC vacío.
 */
fun <T> List<T>.filterBy(query: String, selectors: List<(T) -> String?>): List<T> {
    val needle = query.trim()
    if (needle.isEmpty()) return this
    return filter { item ->
        selectors.any { selector ->
            selector(item)?.contains(needle, ignoreCase = true) == true
        }
    }
}

/** Atajo para el caso de un solo campo. */
fun <T> List<T>.filterBy(query: String, selector: (T) -> String?): List<T> =
    filterBy(query, listOf(selector))
