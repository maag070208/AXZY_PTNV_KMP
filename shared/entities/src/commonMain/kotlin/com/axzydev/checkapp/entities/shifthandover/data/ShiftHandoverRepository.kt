package com.axzydev.checkapp.entities.shifthandover.data

import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.core.common.uuid.randomUuid
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Shift_handovers
import com.axzydev.checkapp.entities.shifthandover.model.HandoverElement
import com.axzydev.checkapp.entities.shifthandover.model.ShiftHandover
import com.axzydev.checkapp.entities.shifthandover.model.ShiftHandoverDraft
import com.axzydev.checkapp.entities.uniformcheck.model.ChecklistAnswer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

interface ShiftHandoverRepository {
    suspend fun save(draft: ShiftHandoverDraft): ShiftHandover
    suspend fun list(limit: Int = 50): List<ShiftHandover>
}

class DefaultShiftHandoverRepository(
    private val database: AxzyCheckDatabase,
    private val timeProvider: TimeProvider,
) : ShiftHandoverRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val checklistSerializer = ListSerializer(ChecklistAnswer.serializer())
    private val elementsSerializer = ListSerializer(HandoverElement.serializer())

    override suspend fun save(draft: ShiftHandoverDraft): ShiftHandover {
        val now = timeProvider.nowEpochMillis()
        val id = randomUuid()

        database.shiftHandoverQueries.insertShiftHandover(
            id = id,
            client_id = draft.clientId,
            schedule_id = draft.scheduleId,
            shift_date = draft.shiftDate,
            credentials_count = draft.credentialsCount?.toLong(),
            tarjetones_count = draft.tarjetonesCount?.toLong(),
            novedades = draft.novedades,
            checklist = json.encodeToString(checklistSerializer, draft.checklist),
            reported_to_admin = if (draft.reportedToAdmin) 1L else 0L,
            created_by_id = draft.createdById,
            elements = json.encodeToString(elementsSerializer, draft.elements),
            created_at = now,
            updated_at = now,
            _status = "created",
        )

        return ShiftHandover(
            id = id,
            clientId = draft.clientId,
            scheduleId = draft.scheduleId,
            shiftDate = draft.shiftDate,
            credentialsCount = draft.credentialsCount,
            tarjetonesCount = draft.tarjetonesCount,
            novedades = draft.novedades,
            checklist = draft.checklist,
            reportedToAdmin = draft.reportedToAdmin,
            createdAt = now,
        )
    }

    override suspend fun list(limit: Int): List<ShiftHandover> =
        database.shiftHandoverQueries.selectShiftHandovers().executeAsList()
            .take(limit)
            .map { it.toModel() }
}

private fun Shift_handovers.toModel(): ShiftHandover = ShiftHandover(
    id = id,
    clientId = client_id,
    scheduleId = schedule_id,
    shiftDate = shift_date,
    credentialsCount = credentials_count?.toInt(),
    tarjetonesCount = tarjetones_count?.toInt(),
    novedades = novedades,
    checklist = emptyList(),
    reportedToAdmin = reported_to_admin != 0L,
    createdAt = created_at,
)
