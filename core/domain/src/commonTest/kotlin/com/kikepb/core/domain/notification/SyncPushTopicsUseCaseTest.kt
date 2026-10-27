package com.kikepb.core.domain.notification

import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.core.domain.featureflag.AppPlatform
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncPushTopicsUseCaseTest {

    private class FakeSubscriber : PushTopicSubscriber {
        val subscribed = mutableListOf<String>()
        val unsubscribed = mutableListOf<String>()
        override suspend fun subscribe(topic: String) = subscribed.add(topic)
        override suspend fun unsubscribe(topic: String) = unsubscribed.add(topic)
    }

    @Test
    fun `AC-021-07 the device joins the topic of its platform and language and leaves the other language`() = runTest {
        // Arrange
        val subscriber = FakeSubscriber()
        val sut = SyncPushTopicsUseCase(subscriber = subscriber, environment = AppEnvironment.PRO, platform = AppPlatform.IOS, localeTag = { "en-GB" })

        // Act
        sut()

        // Assert
        assertEquals(listOf("app_ios_en"), subscriber.subscribed)
        assertEquals(listOf("app_ios_es"), subscriber.unsubscribed)
    }

    @Test
    fun `AC-021-07 any language other than English gets the Spanish notices`() = runTest {
        val subscriber = FakeSubscriber()

        SyncPushTopicsUseCase(subscriber = subscriber, environment = AppEnvironment.PRO, platform = AppPlatform.ANDROID, localeTag = { "ca-ES" })()

        assertEquals(listOf("app_android_es"), subscriber.subscribed)
        assertEquals(listOf("app_android_en"), subscriber.unsubscribed)
    }

    @Test
    fun `AC-021-07 dev builds use their own topics, so a store notice never reaches them`() = runTest {
        val subscriber = FakeSubscriber()

        SyncPushTopicsUseCase(subscriber = subscriber, environment = AppEnvironment.PRE, platform = AppPlatform.ANDROID, localeTag = { "es-ES" })()

        assertEquals(listOf("pre_app_android_es"), subscriber.subscribed)
        assertEquals(listOf("pre_app_android_en"), subscriber.unsubscribed)
    }
}
