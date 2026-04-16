package com.dodamsoft.ajangajang.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.dodamsoft.ajangajang.data.preferences.AppPreferences
import com.dodamsoft.ajangajang.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreferencesViewModel : ViewModel() {

    private val repo = AppContainer.appPreferences()

    // `null` means not yet loaded from DataStore; routing should wait until it emits.
    private val _preferences = MutableStateFlow<AppPreferences?>(null)
    val preferences: StateFlow<AppPreferences?> = _preferences.asStateFlow()

    init {
        viewModelScope.launch {
            repo.preferences.collect { prefs -> _preferences.value = prefs }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { PreferencesViewModel() }
        }
    }
}
