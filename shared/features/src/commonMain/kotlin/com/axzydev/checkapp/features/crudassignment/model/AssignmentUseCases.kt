package com.axzydev.checkapp.features.crudassignment.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.assignment.data.AssignmentRepository
import com.axzydev.checkapp.entities.assignment.model.Assignment
import com.axzydev.checkapp.entities.assignment.model.AssignmentDraft

class ListAssignmentsUseCase(private val repository: AssignmentRepository) {
    suspend operator fun invoke(): List<Assignment> = when (val result = repository.remoteAll()) {
        is ApiResult.Success -> result.data
        is ApiResult.Failure -> repository.localAll()
    }
}

class CreateAssignmentUseCase(private val repository: AssignmentRepository) {
    suspend operator fun invoke(draft: AssignmentDraft): ApiResult<Assignment> = repository.create(draft)
}

class UpdateAssignmentStatusUseCase(private val repository: AssignmentRepository) {
    suspend operator fun invoke(id: String, status: String): ApiResult<Assignment> = repository.updateStatus(id, status)
}

class DeleteAssignmentUseCase(private val repository: AssignmentRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}
