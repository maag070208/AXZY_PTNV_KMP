package com.axzydev.checkapp.features.panic.di

import com.axzydev.checkapp.features.panic.model.FlushPanicQueueUseCase
import com.axzydev.checkapp.features.panic.model.TriggerPanicUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val panicFeatureModule: Module = module {
    factory { TriggerPanicUseCase(get()) }
    factory { FlushPanicQueueUseCase(get()) }
}
