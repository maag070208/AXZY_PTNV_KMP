package com.axzydev.checkapp.features.submitcheck.di

import com.axzydev.checkapp.features.submitcheck.model.SubmitCheckUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val submitCheckModule: Module = module {
    factory { SubmitCheckUseCase(get()) }
}
