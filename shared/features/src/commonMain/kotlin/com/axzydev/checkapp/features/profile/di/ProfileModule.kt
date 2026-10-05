package com.axzydev.checkapp.features.profile.di

import com.axzydev.checkapp.features.profile.model.ChangePasswordUseCase
import com.axzydev.checkapp.features.profile.model.GetProfileUseCase
import com.axzydev.checkapp.features.profile.model.UpdateProfileUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val profileModule: Module = module {
    factory { GetProfileUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { ChangePasswordUseCase(get()) }
}
