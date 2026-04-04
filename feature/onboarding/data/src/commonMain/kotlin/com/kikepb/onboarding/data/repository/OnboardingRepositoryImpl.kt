package com.kikepb.onboarding.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.kikepb.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OnboardingRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : OnboardingRepository {

    private val hasSeenOnboardingKey = booleanPreferencesKey("KEY_HAS_SEEN_ONBOARDING")

    override fun hasSeenOnboarding(): Flow<Boolean> = dataStore.data.map { preferences ->
            preferences[hasSeenOnboardingKey] ?: false
        }

    override suspend fun setOnboardingAsSeen() {
        dataStore.edit { preferences ->
            preferences[hasSeenOnboardingKey] = true
        }
    }
}
