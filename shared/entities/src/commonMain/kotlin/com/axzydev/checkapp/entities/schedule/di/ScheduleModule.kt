package com.axzydev.checkapp.entities.schedule.di

import com.axzydev.checkapp.entities.schedule.data.DefaultScheduleRepository
import com.axzydev.checkapp.entities.schedule.data.ScheduleRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val scheduleModule: Module = module {
    single<ScheduleRepository> { DefaultScheduleRepository(get(), get()) }
}
