package com.axzydev.checkapp.features.cruddiscipline.di

import com.axzydev.checkapp.features.cruddiscipline.model.ListDisciplinesUseCase
import com.axzydev.checkapp.features.cruddiscipline.model.ResolveDisciplineUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val crudDisciplineModule: Module = module {
    factory { ListDisciplinesUseCase(get()) }
    factory { ResolveDisciplineUseCase(get()) }
}
