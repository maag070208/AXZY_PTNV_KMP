package com.axzydev.checkapp.features.crudschedule.di

import com.axzydev.checkapp.features.crudschedule.model.CreateScheduleUseCase
import com.axzydev.checkapp.features.crudschedule.model.DeleteScheduleUseCase
import com.axzydev.checkapp.features.crudschedule.model.ListSchedulesUseCase
import com.axzydev.checkapp.features.crudschedule.model.UpdateScheduleUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val crudScheduleModule: Module = module {
    factory { ListSchedulesUseCase(get()) }
    factory { CreateScheduleUseCase(get()) }
    factory { UpdateScheduleUseCase(get()) }
    factory { DeleteScheduleUseCase(get()) }
}
