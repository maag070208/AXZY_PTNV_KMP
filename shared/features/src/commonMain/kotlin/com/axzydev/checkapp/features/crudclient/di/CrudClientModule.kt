package com.axzydev.checkapp.features.crudclient.di

import com.axzydev.checkapp.features.crudclient.model.CreateClientUseCase
import com.axzydev.checkapp.features.crudclient.model.DeleteClientUseCase
import com.axzydev.checkapp.features.crudclient.model.ListClientsUseCase
import com.axzydev.checkapp.features.crudclient.model.UpdateClientUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val crudClientModule: Module = module {
    factory { ListClientsUseCase(get()) }
    factory { CreateClientUseCase(get()) }
    factory { UpdateClientUseCase(get()) }
    factory { DeleteClientUseCase(get()) }
}
