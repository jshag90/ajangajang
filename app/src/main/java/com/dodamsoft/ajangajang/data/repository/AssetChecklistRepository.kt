package com.dodamsoft.ajangajang.data.repository

import android.content.Context
import com.dodamsoft.ajangajang.domain.model.ChecklistCatalog
import com.dodamsoft.ajangajang.domain.model.ChecklistStage
import com.dodamsoft.ajangajang.domain.repository.ChecklistRepository
import com.dodamsoft.ajangajang.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicReference

class AssetChecklistRepository(
    private val context: Context,
    private val json: Json,
) : ChecklistRepository {

    private val cached = AtomicReference<ChecklistCatalog?>(null)

    override suspend fun loadCatalog(): ChecklistCatalog = withContext(Dispatchers.IO) {
        cached.get() ?: context.assets.open(Constants.CHECKLIST_ASSET).use { stream ->
            val text = stream.bufferedReader(Charsets.UTF_8).readText()
            json.decodeFromString<ChecklistCatalog>(text).also { cached.set(it) }
        }
    }

    override suspend fun getStage(months: Int): ChecklistStage? =
        loadCatalog().stages.firstOrNull { it.months == months }

    override suspend fun getAllStages(): List<ChecklistStage> = loadCatalog().stages
}
