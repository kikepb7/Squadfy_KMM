package com.kikepb.core.data.util

import java.util.Locale

actual fun currentLocaleTag(): String = Locale.getDefault().toLanguageTag()
