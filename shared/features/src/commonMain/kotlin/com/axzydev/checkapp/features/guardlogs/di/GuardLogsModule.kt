package com.axzydev.checkapp.features.guardlogs.di

import com.axzydev.checkapp.features.guardlogs.model.ListGuardLogsUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val guardLogsModule: Module = module {
    factory { ListGuardLogsUseCase(get()) }
}
