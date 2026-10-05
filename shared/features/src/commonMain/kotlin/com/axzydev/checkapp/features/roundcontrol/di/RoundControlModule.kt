package com.axzydev.checkapp.features.roundcontrol.di

import com.axzydev.checkapp.features.roundcontrol.model.EndRoundUseCase
import com.axzydev.checkapp.features.roundcontrol.model.StartRoundUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val roundControlModule: Module = module {
    factory { StartRoundUseCase(get()) }
    factory { EndRoundUseCase(get()) }
}
