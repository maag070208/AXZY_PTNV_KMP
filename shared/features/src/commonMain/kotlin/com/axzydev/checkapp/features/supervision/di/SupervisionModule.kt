package com.axzydev.checkapp.features.supervision.di

import com.axzydev.checkapp.features.supervision.model.GetUniformCatalogUseCase
import com.axzydev.checkapp.features.supervision.model.GetShiftHandoverCatalogUseCase
import com.axzydev.checkapp.features.supervision.model.ListShiftHandoversUseCase
import com.axzydev.checkapp.features.supervision.model.ListUniformChecksUseCase
import com.axzydev.checkapp.features.supervision.model.SaveShiftHandoverUseCase
import com.axzydev.checkapp.features.supervision.model.SaveUniformCheckUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val supervisionModule: Module = module {
    factory { SaveUniformCheckUseCase(get()) }
    factory { ListUniformChecksUseCase(get()) }
    factory { GetUniformCatalogUseCase(get()) }
    factory { GetShiftHandoverCatalogUseCase(get()) }
    factory { SaveShiftHandoverUseCase(get()) }
    factory { ListShiftHandoversUseCase(get()) }
}
