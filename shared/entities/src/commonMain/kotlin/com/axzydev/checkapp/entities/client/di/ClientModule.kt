package com.axzydev.checkapp.entities.client.di

import com.axzydev.checkapp.entities.client.data.ClientRepository
import com.axzydev.checkapp.entities.client.data.DefaultClientRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val clientModule: Module = module {
    single<ClientRepository> { DefaultClientRepository(get(), get()) }
}
