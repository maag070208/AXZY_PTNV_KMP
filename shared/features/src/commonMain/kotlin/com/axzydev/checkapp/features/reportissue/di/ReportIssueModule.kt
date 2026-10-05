package com.axzydev.checkapp.features.reportissue.di

import com.axzydev.checkapp.features.reportissue.model.ListIssueCategoriesUseCase
import com.axzydev.checkapp.features.reportissue.model.ReportIncidentUseCase
import com.axzydev.checkapp.features.reportissue.model.ReportMaintenanceUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val reportIssueModule: Module = module {
    factory { ListIssueCategoriesUseCase(get()) }
    factory { ReportIncidentUseCase(get()) }
    factory { ReportMaintenanceUseCase(get()) }
}
