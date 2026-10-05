package com.axzydev.checkapp.entities.assignment.di

import com.axzydev.checkapp.entities.assignment.data.AssignmentRepository
import com.axzydev.checkapp.entities.assignment.data.DefaultAssignmentRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val assignmentModule: Module = module {
    single<AssignmentRepository> { DefaultAssignmentRepository(get(), get()) }
}
