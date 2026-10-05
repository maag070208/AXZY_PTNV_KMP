package com.axzydev.checkapp.features.crudassignment.di

import com.axzydev.checkapp.features.crudassignment.model.CreateAssignmentUseCase
import com.axzydev.checkapp.features.crudassignment.model.DeleteAssignmentUseCase
import com.axzydev.checkapp.features.crudassignment.model.ListAssignmentsUseCase
import com.axzydev.checkapp.features.crudassignment.model.UpdateAssignmentStatusUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val crudAssignmentModule: Module = module {
    factory { ListAssignmentsUseCase(get()) }
    factory { CreateAssignmentUseCase(get()) }
    factory { UpdateAssignmentStatusUseCase(get()) }
    factory { DeleteAssignmentUseCase(get()) }
}
