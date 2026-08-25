package com.kikepb.core.data.networking

import com.kikepb.core.data.BuildKonfig

/**
 * Values default to the Android emulator loopback (10.0.2.2) for local development. Any real
 * build must override BASE_URL_HTTP/BASE_URL_WS at build time (see [BuildKonfigConventionPlugin]
 * in build-logic), otherwise it will try to reach a backend that doesn't exist on that device.
 */
object UrlConstants {
    val BASE_URL_HTTP = BuildKonfig.BASE_URL_HTTP
    val BASE_URL_WS = BuildKonfig.BASE_URL_WS
}
