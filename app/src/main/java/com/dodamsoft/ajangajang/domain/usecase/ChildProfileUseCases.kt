package com.dodamsoft.ajangajang.domain.usecase

import com.dodamsoft.ajangajang.domain.model.ChildProfile
import com.dodamsoft.ajangajang.domain.repository.CheckRecordRepository
import com.dodamsoft.ajangajang.domain.repository.ChildProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveChildProfilesUseCase(
    private val repository: ChildProfileRepository,
) {
    operator fun invoke(): Flow<List<ChildProfile>> = repository.observeAll()
}

class AddChildProfileUseCase(
    private val profileRepository: ChildProfileRepository,
    private val recordRepository: CheckRecordRepository,
) {
    /**
     * Inserts a new profile. If this is the first profile, marks it primary and
     * silently attaches any pre-existing guest check records (childId IS NULL) to it.
     */
    suspend operator fun invoke(profile: ChildProfile): Long {
        val isFirst = profileRepository.count() == 0
        val toInsert = profile.copy(isPrimary = isFirst || profile.isPrimary)
        val newId = profileRepository.insert(toInsert)
        if (isFirst) {
            recordRepository.attributeGuestRecordsTo(newId)
        }
        return newId
    }
}

class UpdateChildProfileUseCase(
    private val repository: ChildProfileRepository,
) {
    suspend operator fun invoke(profile: ChildProfile) = repository.update(profile)
}

class DeleteChildProfileUseCase(
    private val repository: ChildProfileRepository,
) {
    suspend operator fun invoke(profile: ChildProfile) = repository.delete(profile)
}

class SetPrimaryChildUseCase(
    private val repository: ChildProfileRepository,
) {
    suspend operator fun invoke(id: Long) = repository.setPrimary(id)
}
