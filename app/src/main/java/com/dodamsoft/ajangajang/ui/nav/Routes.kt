package com.dodamsoft.ajangajang.ui.nav

import kotlinx.serialization.Serializable

// Top-level graph routes
@Serializable
object OnboardingRoute

@Serializable
object MainGraphRoute

// Bottom tab routes (nested inside MainGraphRoute)
@Serializable
object HomeTabRoute

@Serializable
object ChecklistTabRoute

@Serializable
object RecordsTabRoute

@Serializable
object SettingsTabRoute

// Full-screen pushed destinations (not tabs)
@Serializable
data class ChecklistRoute(val months: Int)

@Serializable
object ProfileManageRoute
