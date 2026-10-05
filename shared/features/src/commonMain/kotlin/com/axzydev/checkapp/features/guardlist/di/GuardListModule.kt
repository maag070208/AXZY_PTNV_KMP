package com.axzydev.checkapp.features.guardlist.di

import com.axzydev.checkapp.features.guardlist.model.DeleteGuardUseCase
import com.axzydev.checkapp.features.guardlist.model.GetGuardUseCase
import com.axzydev.checkapp.features.guardlist.model.ListGuardsUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val guardListModule: Module = module {
    factory { ListGuardsUseCase(get()) }
    factory { GetGuardUseCase(get()) }
    factory { DeleteGuardUseCase(get()) }
}
