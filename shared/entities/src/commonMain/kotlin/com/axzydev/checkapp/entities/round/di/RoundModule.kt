package com.axzydev.checkapp.entities.round.di

import com.axzydev.checkapp.entities.round.data.DefaultRoundRepository
import com.axzydev.checkapp.entities.round.data.RoundRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val roundModule: Module = module {
    single<RoundRepository> { DefaultRoundRepository(get(), get()) }
}
