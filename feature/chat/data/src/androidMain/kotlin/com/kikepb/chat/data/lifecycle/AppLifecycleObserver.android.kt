package com.kikepb.chat.data.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

actual class AppLifecycleObserver {
    actual val isInForeground: Flow<Boolean> = callbackFlow {
        val lifecycle = ProcessLifecycleOwner.get().lifecycle

        val isAtLeastStarted = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        send(element = isAtLeastStarted)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> trySend(element = true)
                Lifecycle.Event.ON_STOP -> trySend(element = false)
                else -> Unit
            }
        }

        lifecycle.addObserver(observer = observer)

        awaitClose {
            lifecycle.removeObserver(observer = observer)
        }
    }.flowOn(Dispatchers.Main)
}