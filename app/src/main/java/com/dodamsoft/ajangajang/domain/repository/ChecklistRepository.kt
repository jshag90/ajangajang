package com.dodamsoft.ajangajang.domain.repository

import com.dodamsoft.ajangajang.domain.model.ChecklistCatalog
import com.dodamsoft.ajangajang.domain.model.ChecklistStage

interface ChecklistRepository {
    suspend fun loadCatalog(): ChecklistCatalog
    suspend fun getStage(months: Int): ChecklistStage?
    suspend fun getAllStages(): List<ChecklistStage>
}
