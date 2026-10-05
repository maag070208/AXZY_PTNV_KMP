package com.axzydev.checkapp.features.scanqr.di

import com.axzydev.checkapp.features.scanqr.model.MatchLocationUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val scanQrModule: Module = module {
    factory { MatchLocationUseCase(get()) }
}
