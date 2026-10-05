package com.axzydev.checkapp.entities.panic.di

import com.axzydev.checkapp.entities.panic.data.DefaultPanicRepository
import com.axzydev.checkapp.entities.panic.data.PanicRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val panicModule: Module = module {
    single<PanicRepository> { DefaultPanicRepository(get(), get()) }
}
