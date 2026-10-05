package com.axzydev.checkapp.features.supervision.model

import com.axzydev.checkapp.core.datastore.SyncChecklistItem
import com.axzydev.checkapp.entities.synccatalog.data.SyncCatalogRepository
import com.axzydev.checkapp.entities.uniformcheck.data.UniformCheckRepository
import com.axzydev.checkapp.entities.uniformcheck.model.UniformCheck
import com.axzydev.checkapp.entities.uniformcheck.model.UniformCheckDraft
import com.axzydev.checkapp.entities.shifthandover.data.ShiftHandoverRepository
import com.axzydev.checkapp.entities.shifthandover.model.ShiftHandover
import com.axzydev.checkapp.entities.shifthandover.model.ShiftHandoverDraft

class SaveUniformCheckUseCase(private val repository: UniformCheckRepository) {
    suspend operator fun invoke(draft: UniformCheckDraft): UniformCheck = repository.save(draft)
}

class ListUniformChecksUseCase(private val repository: UniformCheckRepository) {
    suspend operator fun invoke(): List<UniformCheck> = repository.list()
}

data class UniformCatalogData(val items: List<SyncChecklistItem>, val minCompliantScore: Int)

class GetUniformCatalogUseCase(private val repository: SyncCatalogRepository) {
    suspend operator fun invoke(): UniformCatalogData? =
        repository.catalogs()?.uniform?.let { UniformCatalogData(it.items, it.minCompliantScore) }
}

class GetShiftHandoverCatalogUseCase(private val repository: SyncCatalogRepository) {
    suspend operator fun invoke(): List<SyncChecklistItem> = repository.catalogs()?.shiftHandover.orEmpty()
}

class SaveShiftHandoverUseCase(private val repository: ShiftHandoverRepository) {
    suspend operator fun invoke(draft: ShiftHandoverDraft): ShiftHandover = repository.save(draft)
}

class ListShiftHandoversUseCase(private val repository: ShiftHandoverRepository) {
    suspend operator fun invoke(): List<ShiftHandover> = repository.list()
}
