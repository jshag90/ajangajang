package com.dodamsoft.ajangajang.data.repository

import com.dodamsoft.ajangajang.data.local.dao.ChildProfileDao
import com.dodamsoft.ajangajang.data.local.entity.ChildProfileEntity
import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.model.Gender
import com.dodamsoft.ajangajang.domain.repository.ChildProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomChildProfileRepository(
    private val dao: ChildProfileDao,
) : ChildProfileRepository {

    override fun observeAll(): Flow<List<ChildProfile>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAll(): List<ChildProfile> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): ChildProfile? =
        dao.getById(id)?.toDomain()

    override suspend fun count(): Int = dao.count()

    override suspend fun insert(profile: ChildProfile): Long =
        dao.insert(profile.toEntity())

    override suspend fun update(profile: ChildProfile) {
        dao.update(profile.toEntity())
    }

    override suspend fun delete(profile: ChildProfile) {
        dao.delete(profile.toEntity())
    }

    override suspend fun setPrimary(id: Long) {
        dao.setPrimary(id)
    }
}

private fun ChildProfileEntity.toDomain(): ChildProfile = ChildProfile(
    id = id,
    name = name,
    birthDate = LocalDate.ofEpochDay(birthDateEpochDay),
    gender = runCatching { Gender.valueOf(gender) }.getOrDefault(Gender.UNKNOWN),
    photoUri = photoUri,
    isPrimary = isPrimary,
    createdAt = createdAt,
)

private fun ChildProfile.toEntity(): ChildProfileEntity = ChildProfileEntity(
    id = id,
    name = name,
    birthDateEpochDay = birthDate.toEpochDay(),
    gender = gender.name,
    photoUri = photoUri,
    isPrimary = isPrimary,
    createdAt = createdAt,
)
