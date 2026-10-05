package com.axzydev.checkapp.core.sync

/** Correspondencia entre tablas locales (SQLDelight) y modelos de la API (Prisma). */
object SyncMappings {

    val localToApi: Map<String, String> = mapOf(
        "roles" to "role",
        "clients" to "client",
        "zones" to "zone",
        "users" to "user",
        "schedules" to "schedule",
        "locations" to "location",
        "location_tasks" to "locationTask",
        "kardex" to "kardex",
        "assignments" to "assignment",
        "assignment_tasks" to "assignmentTask",
        "incident_categories" to "incidentCategory",
        "incident_types" to "incidentType",
        "incidents" to "incident",
        "rounds" to "round",
        "maintenances" to "maintenance",
        "recurring_configurations" to "recurringConfiguration",
        "recurring_locations" to "recurringLocation",
        "recurring_tasks" to "recurringTask",
        "shift_handovers" to "shiftHandover",
        "uniform_checks" to "uniformCheck",
    )

    val apiToLocal: Map<String, String> =
        localToApi.entries.associate { (local, api) -> api to local }

    val displayNames: Map<String, String> = mapOf(
        "role" to "Roles",
        "client" to "Clientes",
        "zone" to "Zonas",
        "user" to "Personal / Guardias",
        "schedule" to "Horarios",
        "location" to "Puntos de control",
        "locationTask" to "Tareas de ubicación",
        "kardex" to "Registros de bitácora",
        "assignment" to "Asignaciones",
        "assignmentTask" to "Tareas asignadas",
        "incidentCategory" to "Categorías de incidencias",
        "incidentType" to "Tipos de incidencias",
        "incident" to "Reportes de incidencias",
        "round" to "Recorridos / Rondas",
        "maintenance" to "Reportes de mantenimiento",
        "recurringConfiguration" to "Rondas recurrentes",
        "recurringLocation" to "Puntos recurrentes",
        "recurringTask" to "Tareas recurrentes",
        "shiftHandover" to "Entregas de turno",
        "uniformCheck" to "Revisiones de uniforme",
    )

    /** Tablas locales re-descargadas completas cuando están vacías (`reset_models`). */
    val resetLocalTables: List<String> = RESET_MODELS.mapNotNull { apiToLocal[it] }

    fun apiModelFor(local: String): String? = localToApi[local]

    fun localFor(api: String): String? = apiToLocal[api]

    fun displayNameFor(api: String): String = displayNames[api] ?: api
}
