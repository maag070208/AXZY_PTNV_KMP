package com.axzydev.checkapp.features.crudrecurring.di

import com.axzydev.checkapp.features.crudrecurring.model.CreateRecurringUseCase
import com.axzydev.checkapp.features.crudrecurring.model.DeleteRecurringUseCase
import com.axzydev.checkapp.features.crudrecurring.model.GetRecurringUseCase
import com.axzydev.checkapp.features.crudrecurring.model.ListRecurringUseCase
import com.axzydev.checkapp.features.crudrecurring.model.UpdateRecurringUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val crudRecurringModule: Module = module {
    factory { ListRecurringUseCase(get()) }
    factory { GetRecurringUseCase(get()) }
    factory { CreateRecurringUseCase(get()) }
    factory { UpdateRecurringUseCase(get()) }
    factory { DeleteRecurringUseCase(get()) }
}
