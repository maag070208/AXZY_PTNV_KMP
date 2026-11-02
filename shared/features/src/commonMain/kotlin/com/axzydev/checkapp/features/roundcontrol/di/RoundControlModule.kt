package com.axzydev.checkapp.features.roundcontrol.di

import com.axzydev.checkapp.features.roundcontrol.model.CloseAbandonedRoundUseCase
import com.axzydev.checkapp.features.roundcontrol.model.EndRoundUseCase
import com.axzydev.checkapp.features.roundcontrol.model.HasActiveRoundUseCase
import com.axzydev.checkapp.features.roundcontrol.model.StartRoundUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val roundControlModule: Module = module {
    factory { StartRoundUseCase(get()) }
    factory { EndRoundUseCase(get()) }
    factory { HasActiveRoundUseCase(get(), get()) }
    factory { CloseAbandonedRoundUseCase(get(), get(), get()) }
}
