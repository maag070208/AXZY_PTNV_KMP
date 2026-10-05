package com.axzydev.checkapp.features.crudlocation.di

import com.axzydev.checkapp.features.crudlocation.model.CreateLocationUseCase
import com.axzydev.checkapp.features.crudlocation.model.DeleteLocationUseCase
import com.axzydev.checkapp.features.crudlocation.model.ListLocationsUseCase
import com.axzydev.checkapp.features.crudlocation.model.UpdateLocationUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val crudLocationModule: Module = module {
    factory { ListLocationsUseCase(get()) }
    factory { CreateLocationUseCase(get()) }
    factory { UpdateLocationUseCase(get()) }
    factory { DeleteLocationUseCase(get()) }
}
