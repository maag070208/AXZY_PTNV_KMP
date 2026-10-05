package com.axzydev.checkapp.entities.shifthandover.di

import com.axzydev.checkapp.entities.shifthandover.data.DefaultShiftHandoverRepository
import com.axzydev.checkapp.entities.shifthandover.data.ShiftHandoverRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val shiftHandoverModule: Module = module {
    single<ShiftHandoverRepository> { DefaultShiftHandoverRepository(get(), get()) }
}
