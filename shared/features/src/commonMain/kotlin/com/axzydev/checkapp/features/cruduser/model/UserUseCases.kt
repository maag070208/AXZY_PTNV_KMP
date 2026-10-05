package com.axzydev.checkapp.features.cruduser.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.role.data.RoleRepository
import com.axzydev.checkapp.entities.role.model.Role
import com.axzydev.checkapp.entities.user.data.UserRepository
import com.axzydev.checkapp.entities.user.model.User
import com.axzydev.checkapp.entities.user.model.UserDraft
import com.axzydev.checkapp.entities.user.model.UserUpdateDraft

class ListUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): List<User> = when (val result = repository.remoteAll()) {
        is ApiResult.Success -> result.data
        is ApiResult.Failure -> repository.localAll()
    }
}

class CreateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(draft: UserDraft): ApiResult<User> = repository.create(draft)
}

class UpdateUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String, draft: UserUpdateDraft): ApiResult<User> = repository.update(id, draft)
}

class DeleteUserUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}

class ListRolesUseCase(private val repository: RoleRepository) {
    suspend operator fun invoke(): List<Role> = repository.localAll()
}
