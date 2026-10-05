package com.axzydev.checkapp.features.crudclient.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.client.data.ClientRepository
import com.axzydev.checkapp.entities.client.model.Client
import com.axzydev.checkapp.entities.client.model.ClientDraft

/** Lista clientes: remoto si hay red; si falla, cae a la copia local. */
class ListClientsUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(): List<Client> {
        return when (val result = repository.remoteAll()) {
            is ApiResult.Success -> result.data
            is ApiResult.Failure -> repository.localAll()
        }
    }
}

class CreateClientUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(draft: ClientDraft): ApiResult<Client> = repository.create(draft)
}

class UpdateClientUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(id: String, draft: ClientDraft): ApiResult<Client> = repository.update(id, draft)
}

class DeleteClientUseCase(private val repository: ClientRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}
