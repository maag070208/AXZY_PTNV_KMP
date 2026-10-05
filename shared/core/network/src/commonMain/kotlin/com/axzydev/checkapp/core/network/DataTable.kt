package com.axzydev.checkapp.core.network

import kotlinx.serialization.Serializable

/** Parámetros de un endpoint tipo datatable (paginación, filtros, orden). */
@Serializable
data class DataTableParams(
    val page: Int = 1,
    val limit: Int = 15,
    val filters: Map<String, String> = emptyMap(),
    val sort: Map<String, String> = emptyMap(),
)

/** Respuesta paginada tipo datatable. */
@Serializable
data class DatatableDto<T>(
    val rows: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 15,
)
