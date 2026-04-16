package com.dodamsoft.ajangajang.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dodamsoft.ajangajang.data.local.AjangDatabase
import com.dodamsoft.ajangajang.data.preferences.AppPreferencesRepository
import com.dodamsoft.ajangajang.data.repository.AssetChecklistRepository
import com.dodamsoft.ajangajang.data.repository.RoomCheckRecordRepository
import com.dodamsoft.ajangajang.data.repository.RoomChildProfileRepository
import com.dodamsoft.ajangajang.domain.repository.CheckRecordRepository
import com.dodamsoft.ajangajang.domain.repository.ChecklistRepository
import com.dodamsoft.ajangajang.domain.repository.ChildProfileRepository
import com.dodamsoft.ajangajang.domain.usecase.AddChildProfileUseCase
import com.dodamsoft.ajangajang.domain.usecase.CalculateResultUseCase
import com.dodamsoft.ajangajang.domain.usecase.DeleteChildProfileUseCase
import com.dodamsoft.ajangajang.domain.usecase.GetAllStagesUseCase
import com.dodamsoft.ajangajang.domain.usecase.GetChecklistStageUseCase
import com.dodamsoft.ajangajang.domain.usecase.GetRecentCheckRecordsUseCase
import com.dodamsoft.ajangajang.domain.usecase.ObserveAllCheckRecordsUseCase
import com.dodamsoft.ajangajang.domain.usecase.ObserveChildProfilesUseCase
import com.dodamsoft.ajangajang.domain.usecase.SaveCheckRecordUseCase
import com.dodamsoft.ajangajang.domain.usecase.SetPrimaryChildUseCase
import com.dodamsoft.ajangajang.domain.usecase.UpdateChildProfileUseCase
import com.dodamsoft.ajangajang.ui.checklist.ChecklistViewModel
import com.dodamsoft.ajangajang.ui.checklist.StageSelectViewModel
import com.dodamsoft.ajangajang.ui.home.HomeViewModel
import com.dodamsoft.ajangajang.ui.onboarding.OnboardingViewModel
import com.dodamsoft.ajangajang.ui.profile.ProfileViewModel
import com.dodamsoft.ajangajang.ui.records.RecordsViewModel
import kotlinx.serialization.json.Json

object AppContainer {
    private lateinit var appContext: Context

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    // Data sources
    private val database: AjangDatabase by lazy { AjangDatabase.get(appContext) }

    // Repositories
    private val checklistRepository: ChecklistRepository by lazy {
        AssetChecklistRepository(appContext, json)
    }
    private val childProfileRepository: ChildProfileRepository by lazy {
        RoomChildProfileRepository(database.childProfileDao())
    }
    private val checkRecordRepository: CheckRecordRepository by lazy {
        RoomCheckRecordRepository(database.checkRecordDao())
    }
    private val appPreferencesRepository: AppPreferencesRepository by lazy {
        AppPreferencesRepository(appContext)
    }

    // Use cases
    private val getAllStagesUseCase by lazy { GetAllStagesUseCase(checklistRepository) }
    private val getChecklistStageUseCase by lazy { GetChecklistStageUseCase(checklistRepository) }
    private val calculateResultUseCase by lazy { CalculateResultUseCase() }
    private val observeChildProfilesUseCase by lazy { ObserveChildProfilesUseCase(childProfileRepository) }
    private val addChildProfileUseCase by lazy {
        AddChildProfileUseCase(childProfileRepository, checkRecordRepository)
    }
    private val updateChildProfileUseCase by lazy { UpdateChildProfileUseCase(childProfileRepository) }
    private val deleteChildProfileUseCase by lazy { DeleteChildProfileUseCase(childProfileRepository) }
    private val setPrimaryChildUseCase by lazy { SetPrimaryChildUseCase(childProfileRepository) }
    private val saveCheckRecordUseCase by lazy { SaveCheckRecordUseCase(checkRecordRepository) }
    private val observeAllCheckRecordsUseCase by lazy { ObserveAllCheckRecordsUseCase(checkRecordRepository) }
    private val getRecentCheckRecordsUseCase by lazy { GetRecentCheckRecordsUseCase(checkRecordRepository) }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun appPreferences(): AppPreferencesRepository = appPreferencesRepository

    // ViewModel factories
    fun stageSelectVmFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { StageSelectViewModel(getAllStagesUseCase) }
    }

    fun checklistVmFactory(months: Int): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ChecklistViewModel(
                months = months,
                getStage = getChecklistStageUseCase,
                calculateResult = calculateResultUseCase,
                saveCheckRecord = saveCheckRecordUseCase,
                appPreferences = appPreferencesRepository,
                observeChildProfiles = observeChildProfilesUseCase,
            )
        }
    }

    fun homeVmFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                observeChildProfiles = observeChildProfilesUseCase,
                observeAllRecords = observeAllCheckRecordsUseCase,
                appPreferences = appPreferencesRepository,
                setPrimaryChild = setPrimaryChildUseCase,
            )
        }
    }

    fun recordsVmFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            RecordsViewModel(
                observeAll = observeAllCheckRecordsUseCase,
                observeChildProfiles = observeChildProfilesUseCase,
                getStage = getChecklistStageUseCase,
                calculateResult = calculateResultUseCase,
            )
        }
    }

    fun profileVmFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ProfileViewModel(
                observeChildProfiles = observeChildProfilesUseCase,
                addChild = addChildProfileUseCase,
                updateChild = updateChildProfileUseCase,
                deleteChild = deleteChildProfileUseCase,
                setPrimary = setPrimaryChildUseCase,
                appPreferences = appPreferencesRepository,
            )
        }
    }

    fun onboardingVmFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer { OnboardingViewModel(appPreferencesRepository) }
    }
}
