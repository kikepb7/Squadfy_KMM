package com.kikepb.chat.datafirebase.repository

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

internal actual fun currentLocaleTag(): String = NSLocale.currentLocale.localeIdentifier.replace('_', '-')
