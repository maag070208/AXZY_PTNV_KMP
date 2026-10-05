package com.axzydev.checkapp.entities.uniformcheck.data

import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.core.common.uuid.randomUuid
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Uniform_checks
import com.axzydev.checkapp.entities.uniformcheck.model.ChecklistAnswer
import com.axzydev.checkapp.entities.uniformcheck.model.UniformCheck
import com.axzydev.checkapp.entities.uniformcheck.model.UniformCheckDraft
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

interface UniformCheckRepository {
    suspend fun save(draft: UniformCheckDraft): UniformCheck
    suspend fun list(limit: Int = 50): List<UniformCheck>
}

class DefaultUniformCheckRepository(
    private val database: AxzyCheckDatabase,
    private val timeProvider: TimeProvider,
) : UniformCheckRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val itemsSerializer = ListSerializer(ChecklistAnswer.serializer())

    override suspend fun save(draft: UniformCheckDraft): UniformCheck {
        val now = timeProvider.nowEpochMillis()
        val id = randomUuid()
        val score = if (draft.items.isEmpty()) 0 else draft.items.count { it.ok } * 100 / draft.items.size
        val compliant = score >= draft.minCompliantScore

        database.uniformCheckQueries.insertUniformCheck(
            id = id,
            guard_id = draft.guardId,
            client_id = draft.clientId,
            schedule_id = draft.scheduleId,
            shift_date = draft.shiftDate,
            evaluated_by_id = draft.evaluatedById,
            items = json.encodeToString(itemsSerializer, draft.items),
            score = score.toLong(),
            compliant = if (compliant) 1L else 0L,
            notes = draft.notes,
            created_at = now,
            updated_at = now,
            _status = "created",
        )

        return UniformCheck(id, draft.guardId, draft.shiftDate, score, compliant, draft.notes, now)
    }

    override suspend fun list(limit: Int): List<UniformCheck> =
        database.uniformCheckQueries.selectUniformChecks().executeAsList()
            .take(limit)
            .map { it.toModel() }
}

private fun Uniform_checks.toModel(): UniformCheck = UniformCheck(
    id = id,
    guardId = guard_id,
    shiftDate = shift_date ?: "",
    score = score.toInt(),
    compliant = compliant != 0L,
    notes = notes,
    createdAt = created_at,
)
