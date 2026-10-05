package com.axzydev.checkapp.entities.guardlog.di

import com.axzydev.checkapp.entities.guardlog.data.DefaultGuardLogRepository
import com.axzydev.checkapp.entities.guardlog.data.GuardLogRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val guardLogModule: Module = module {
    single<GuardLogRepository> { DefaultGuardLogRepository(get()) }
}
