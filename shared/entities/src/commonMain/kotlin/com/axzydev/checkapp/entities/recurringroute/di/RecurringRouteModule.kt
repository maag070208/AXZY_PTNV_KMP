package com.axzydev.checkapp.entities.recurringroute.di

import com.axzydev.checkapp.entities.recurringroute.data.DefaultRecurringRouteRepository
import com.axzydev.checkapp.entities.recurringroute.data.RecurringRouteRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val recurringRouteModule: Module = module {
    single<RecurringRouteRepository> { DefaultRecurringRouteRepository(get(), get()) }
}
