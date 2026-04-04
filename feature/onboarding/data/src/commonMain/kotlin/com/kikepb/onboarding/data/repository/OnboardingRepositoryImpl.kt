package com.kikepb.onboarding.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val sessionStorage: SessionStorage
) : OnboardingRepository {

    override fun hasSeenOnboarding(): Flow<Boolean> =
        sessionStorage.observeAuthInfo().flatMapLatest { authInfo ->
            val userId = authInfo?.user?.id ?: return@flatMapLatest flowOf(false)

            dataStore.data.map { prefs ->
                prefs[onboardingKey(userId)] ?: false
            }
        }

    override suspend fun setOnboardingAsSeen() {
        val userId = sessionStorage.observeAuthInfo().firstOrNull()?.user?.id ?: return
        dataStore.edit { prefs ->
            prefs[onboardingKey(userId)] = true
        }
    }

    private fun onboardingKey(userId: String) = booleanPreferencesKey(name = "HAS_SEEN_ONBOARDING_$userId")
}
