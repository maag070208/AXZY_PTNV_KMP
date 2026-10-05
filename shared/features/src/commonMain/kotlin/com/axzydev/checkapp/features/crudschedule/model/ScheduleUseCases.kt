package com.axzydev.checkapp.features.crudschedule.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.schedule.data.ScheduleRepository
import com.axzydev.checkapp.entities.schedule.model.Schedule
import com.axzydev.checkapp.entities.schedule.model.ScheduleDraft

class ListSchedulesUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(): List<Schedule> = when (val result = repository.remoteAll()) {
        is ApiResult.Success -> result.data
        is ApiResult.Failure -> repository.localAll()
    }
}

class CreateScheduleUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(draft: ScheduleDraft): ApiResult<Schedule> = repository.create(draft)
}

class UpdateScheduleUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(id: String, draft: ScheduleDraft): ApiResult<Schedule> = repository.update(id, draft)
}

class DeleteScheduleUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}
