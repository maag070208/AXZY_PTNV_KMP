package com.axzydev.checkapp.entities.uniformcheck.di

import com.axzydev.checkapp.entities.uniformcheck.data.DefaultUniformCheckRepository
import com.axzydev.checkapp.entities.uniformcheck.data.UniformCheckRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val uniformCheckModule: Module = module {
    single<UniformCheckRepository> { DefaultUniformCheckRepository(get(), get()) }
}
