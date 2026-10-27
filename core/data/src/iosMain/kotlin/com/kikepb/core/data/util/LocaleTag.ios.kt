package com.kikepb.core.data.util

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

actual fun currentLocaleTag(): String = NSLocale.currentLocale.localeIdentifier.replace('_', '-')
