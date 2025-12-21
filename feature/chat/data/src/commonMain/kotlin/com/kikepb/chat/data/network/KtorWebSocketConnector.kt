@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.kikepb.chat.data.network

import com.kikepb.chat.data.dto.websocket.WebSocketMessageDTO
import com.kikepb.chat.data.lifecycle.AppLifecycleObserver
import com.kikepb.chat.data.utils.CAPACITY
import com.kikepb.chat.data.utils.STOP_TIMEOUT_MILLIS
import com.kikepb.chat.domain.error.ConnectionErrorModel
import com.kikepb.chat.domain.error.ConnectionErrorModel.MESSAGE_SEND_FAILED
import com.kikepb.chat.domain.error.ConnectionErrorModel.NOT_CONNECTED
import com.kikepb.chat.domain.models.ConnectionStateModel.CONNECTED
import com.kikepb.chat.domain.models.ConnectionStateModel.CONNECTING
import com.kikepb.chat.domain.models.ConnectionStateModel.DISCONNECTED
import com.kikepb.chat.domain.models.ConnectionStateModel.ERROR_NETWORK
import com.kikepb.core.data.networking.UrlConstants.BASE_URL_WS
import com.kikepb.core.domain.auth.repository.SessionStorage
import com.kikepb.core.domain.logger.SquadfyLogger
import com.kikepb.core.domain.util.EmptyResult
import com.kikepb.core.domain.util.Result
import com.kikepb.feature.chat.data.BuildKonfig.API_KEY
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.websocket.Frame.Ping
import io.ktor.websocket.Frame.Pong
import io.ktor.websocket.Frame.Text
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.seconds

class KtorWebSocketConnector(
    private val httpClient: HttpClient,
    private val applicationScope: CoroutineScope,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val connectionErrorHandler: ConnectionErrorHandler,
    private val connectionRetryHandler: ConnectionRetryHandler,
    private val appLifecycleObserver: AppLifecycleObserver,
    private val connectivityObserver: ConnectivityObserver,
    private val logger: SquadfyLogger
) {
    private val _connectionState = MutableStateFlow(value = DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private var currentSession: WebSocketSession? = null

    private val isConnected = connectivityObserver
        .isConnected
        .debounce(timeout = 1.seconds)
        .stateIn(
            applicationScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = STOP_TIMEOUT_MILLIS),
            false
        )

    private val isInForeground = appLifecycleObserver
        .isInForeground
        .onEach { isInForeground ->
            if (isInForeground) connectionRetryHandler.resetDelay()
        }
        .stateIn(
            applicationScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = STOP_TIMEOUT_MILLIS),
            false
        )

    val messages = combine(
        sessionStorage.observeAuthInfo(),
        isConnected,
        isInForeground
    ) { authInfo, isConnected, isInForeground ->
        when {
            authInfo == null -> {
                logger.info(message = "No authentication details. Clearing session and disconnecting ...")
                _connectionState.value = DISCONNECTED
                currentSession?.close()
                currentSession = null
                connectionRetryHandler.resetDelay()
                null
            }

            !isInForeground -> {
                logger.info(message = "App in background, disconnecting socket proactively ...")
                _connectionState.value = DISCONNECTED
                currentSession?.close()
                currentSession = null
                null
            }

            !isConnected -> {
                logger.info(message = "Device is disconnected, closing WebSocket connection ...")
                _connectionState.value = ERROR_NETWORK
                currentSession?.close()
                currentSession = null
                null
            }

            else -> {
                logger.info(message = "App in foreground and connected. Establishing connection ...")

                if (_connectionState.value !in listOf(CONNECTING, CONNECTED)) _connectionState.value = CONNECTING

                authInfo
            }
        }
    }.flatMapLatest { authInfo ->
        if (authInfo == null) emptyFlow()
        else {
            createWebSocketFlow(authInfo.accessToken)
                // Catch block to transform exceptions for platform compatibility
                .catch { e ->
                    logger.error(message = "Exception in WebSocket", e)

                    currentSession?.close()
                    currentSession = null

                    val transformedException = connectionErrorHandler.transformException(exception = e)
                    throw transformedException
                }
                .retryWhen { t, attempt ->
                    logger.info(message = "Connection failed on attempt $attempt")

                    val shouldRetry = connectionRetryHandler.shouldRetry(cause = t, attempt = attempt)

                    if (shouldRetry) {
                        _connectionState.value = CONNECTING
                        connectionRetryHandler.applyRetryDelay(attempt = attempt)
                    }

                    shouldRetry
                }

                // Catch block for non-retriable errors
                .catch { e ->
                    logger.error(message = "Unhandled WebSocket error", e)

                    _connectionState.value = connectionErrorHandler.getConnectionStateForError(cause = e)
                }
        }
    }

    private fun createWebSocketFlow(accessToken: String) = callbackFlow {
        _connectionState.value = CONNECTING

        currentSession = httpClient.webSocketSession(
            urlString = "${BASE_URL_WS}/chat"
        ) {
            header("Authorization", "Bearer $accessToken")
            header("X-API-Key", API_KEY)
        }

        currentSession?.let { session ->
            _connectionState.value = CONNECTED

            session
                .incoming
                .consumeAsFlow()
                .buffer(capacity = CAPACITY)
                .collect { frame ->
                    when (frame) {
                        is Text -> {
                            val text = frame.readText()
                            logger.info(message = "Received raw text frame: $text")

                            val messageDto = json.decodeFromString<WebSocketMessageDTO>(text)
                            send(element = messageDto)
                        }

                        is Ping -> {
                            logger.debug(message = "Received ping from server. Sending pong ...")
                            session.send(frame = Pong(data = frame.data))
                        }

                        else -> Unit
                    }
                }
        } ?: throw Exception(message = "Failed to establish WebSocket connection")

        awaitClose {
            launch(context = NonCancellable) {
                logger.info(message = "Disconnecting from WebSocket session ...")
                _connectionState.value = DISCONNECTED
                currentSession?.close()
                currentSession = null
            }
        }
    }

    suspend fun sendMessage(message: String): EmptyResult<ConnectionErrorModel> {
        val connectionState = connectionState.value
        if (currentSession == null || connectionState != CONNECTED) Result.Failure(error = NOT_CONNECTED)

        return try {
            currentSession?.send(content = message)
            Result.Success(Unit)
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            logger.error(message = "Unable to send WebSocket message", e)
            Result.Failure(MESSAGE_SEND_FAILED)
        }
    }
}