package com.axzydev.checkapp.entities.guarddiscipline.di

import com.axzydev.checkapp.entities.guarddiscipline.data.DefaultGuardDisciplineRepository
import com.axzydev.checkapp.entities.guarddiscipline.data.GuardDisciplineRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val guardDisciplineModule: Module = module {
    single<GuardDisciplineRepository> { DefaultGuardDisciplineRepository(get()) }
}
