package com.axzydev.checkapp.entities.kardex.di

import com.axzydev.checkapp.entities.kardex.data.DefaultKardexRepository
import com.axzydev.checkapp.entities.kardex.data.KardexRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val kardexModule: Module = module {
    single<KardexRepository> { DefaultKardexRepository(get(), get()) }
}
