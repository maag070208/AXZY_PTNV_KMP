package com.axzydev.checkapp.app.navigation

import com.axzydev.checkapp.entities.session.model.UserRole
import kotlinx.serialization.Serializable

/**
 * Destinos de navegación type-safe. Solo `:shared:app` conoce navegación.
 */
@Serializable
data object GetStartedDestination

@Serializable
data object LoginDestination

@Serializable
data object AdminHomeDestination

@Serializable
data object GuardHomeDestination

@Serializable
data object SyncDestination

@Serializable
data class CheckReportDestination(val roundId: String, val locationId: String)

@Serializable
data class RoundDetailDestination(val roundId: String)

@Serializable
data object KardexDestination

@Serializable
data object ClientsDestination

@Serializable
data class ZonesDestination(val clientId: String)

@Serializable
data object LocationsDestination

@Serializable
data object UsersDestination

@Serializable
data object SchedulesDestination

@Serializable
data class ClientDetailDestination(val clientId: String)

@Serializable
data object GuardsDestination

@Serializable
data class GuardDetailDestination(val guardId: String)

@Serializable
data object RecurringDestination

@Serializable
data class RecurringFormDestination(val routeId: String? = null)

@Serializable
data object AssignmentsDestination

@Serializable
data object IncidentsDestination

@Serializable
data object MaintenanceDestination

@Serializable
data object GuardDisciplineDestination

@Serializable
data object GuardLogsDestination

@Serializable
data object UniformCheckDestination

@Serializable
data object ShiftHandoverDestination

@Serializable
data object IncidentReportDestination

@Serializable
data object MaintenanceReportDestination

@Serializable
data object BulkPrintDestination

@Serializable
data object ProfileDestination

/**
 * @param afterAuth viene del flujo de entrada (login o sesión restaurada): ofrece
 *   continuar al inicio. Desde el menú se visita y se sale, sin botón de continuar.
 *   Va en el destino y no en memoria para que sobreviva a que el sistema mate la app.
 */
@Serializable
data class PermissionsDestination(val afterAuth: Boolean = false)

@Serializable
data object NotificationsDestination

@Serializable
data object SendNotificationDestination

@Serializable
data object ScheduledNotificationsDestination

fun startDestinationFor(role: UserRole): Any =
    if (role.isGuard) GuardHomeDestination else AdminHomeDestination
