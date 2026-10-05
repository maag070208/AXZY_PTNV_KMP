package com.axzydev.checkapp.entities.client.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Clients
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.client.model.Client
import com.axzydev.checkapp.entities.client.model.ClientDraft
import kotlinx.serialization.Serializable

@Serializable
private data class ClientDto(
    val id: String,
    val name: String,
    val address: String? = null,
    val rfc: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val active: Boolean = true,
)

@Serializable
private data class CreateClientRequest(
    val name: String,
    val address: String? = null,
    val rfc: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val appUsername: String? = null,
    val appPassword: String? = null,
    val active: Boolean = true,
)

@Serializable
private data class UpdateClientRequest(
    val name: String,
    val address: String? = null,
    val rfc: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
)

interface ClientRepository {
    /** Lista remota (online). */
    suspend fun remoteAll(): ApiResult<List<Client>>
    /** Crea un cliente en el servidor (online). */
    suspend fun create(draft: ClientDraft): ApiResult<Client>
    /** Actualiza un cliente en el servidor (online). */
    suspend fun update(id: String, draft: ClientDraft): ApiResult<Client>
    /** Baja lógica de un cliente en el servidor (online). */
    suspend fun delete(id: String): ApiResult<Unit>
    /** Listado local (SQLite, disponible offline). */
    suspend fun localAll(): List<Client>
}

class DefaultClientRepository(
    private val api: ApiClient,
    private val database: AxzyCheckDatabase,
) : ClientRepository {

    override suspend fun remoteAll(): ApiResult<List<Client>> =
        api.get<List<ClientDto>>("/clients").map { rows -> rows.map { it.toModel() } }

    override suspend fun create(draft: ClientDraft): ApiResult<Client> =
        api.post<ClientDto>(
            "/clients",
            CreateClientRequest(
                name = draft.name,
                address = draft.address,
                rfc = draft.rfc,
                contactName = draft.contactName,
                contactPhone = draft.contactPhone,
                appUsername = draft.appUsername,
                appPassword = draft.appPassword,
            ),
        ).map { it.toModel() }

    override suspend fun update(id: String, draft: ClientDraft): ApiResult<Client> =
        api.put<ClientDto>(
            "/clients/$id",
            UpdateClientRequest(
                name = draft.name,
                address = draft.address,
                rfc = draft.rfc,
                contactName = draft.contactName,
                contactPhone = draft.contactPhone,
            ),
        ).map { it.toModel() }

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val result = api.delete<Boolean>("/clients/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }

    override suspend fun localAll(): List<Client> =
        database.clientQueries.selectAllClients().executeAsList().map { it.toModel() }
}

private fun ClientDto.toModel(): Client = Client(id, name, address, rfc, contactName, contactPhone, active)

private fun Clients.toModel(): Client =
    Client(id, name, address, rfc, contact_name, contact_phone, active != 0L)
