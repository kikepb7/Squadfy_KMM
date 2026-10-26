package com.kikepb.chat.datafirebase.repository

import java.util.Locale

internal actual fun currentLocaleTag(): String = Locale.getDefault().toLanguageTag()
