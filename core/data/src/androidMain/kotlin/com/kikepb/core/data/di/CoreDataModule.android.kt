package com.kikepb.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.kikepb.core.data.auth.createDataStore
import com.kikepb.core.data.auth.storage.KeystoreSessionCipher
import com.kikepb.core.data.auth.storage.SessionCipher
import com.kikepb.core.data.crash.FirebaseCrashReporter
import com.kikepb.core.domain.crash.CrashReporter
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformCoreDataModule = module {
    single<DataStore<Preferences>> {
        createDataStore(androidContext())
    }
}

actual val platformCoreApiDataModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<SessionCipher> { KeystoreSessionCipher() }
}

actual val platformCoreApiTelemetryModule = module {
    single<CrashReporter> { FirebaseCrashReporter() }
}
