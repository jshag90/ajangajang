package com.dodamsoft.ajangajang.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dodamsoft.ajangajang.data.preferences.AppPreferencesRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val appPreferences: AppPreferencesRepository,
) : ViewModel() {

    fun complete() {
        viewModelScope.launch {
            appPreferences.setOnboardingCompleted(true)
        }
    }
}
