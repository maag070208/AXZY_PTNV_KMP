package com.axzydev.checkapp.features.crudzone.di

import com.axzydev.checkapp.features.crudzone.model.CreateZoneUseCase
import com.axzydev.checkapp.features.crudzone.model.DeleteZoneUseCase
import com.axzydev.checkapp.features.crudzone.model.ListZonesUseCase
import com.axzydev.checkapp.features.crudzone.model.UpdateZoneUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val crudZoneModule: Module = module {
    factory { ListZonesUseCase(get()) }
    factory { CreateZoneUseCase(get()) }
    factory { UpdateZoneUseCase(get()) }
    factory { DeleteZoneUseCase(get()) }
}
