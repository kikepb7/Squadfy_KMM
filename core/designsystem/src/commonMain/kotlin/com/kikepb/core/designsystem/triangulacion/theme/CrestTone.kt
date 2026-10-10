package com.kikepb.core.designsystem.triangulacion.theme

import androidx.compose.ui.graphics.Color

/**
 * The only place where a club gets its own colour: the six crest tones of the kit (background, letter). The
 * backend has no club colour yet, so the app derives a stable tone from the club id ([forKey]).
 */
enum class CrestTone(val background: Color, val content: Color) {
    VOLT(Color(0xFF0E1A12), Color(0xFFC5F65A)),
    CANCHA(Color(0xFF0F4D36), Color(0xFFF6F5EE)),
    BRASA(Color(0xFFFF8A63), Color(0xFF1C0D07)),
    ARCILLA(Color(0xFFE6D3B0), Color(0xFF3A2A12)),
    CIELO(Color(0xFFA8D8EA), Color(0xFF0D2A36)),
    AMBAR(Color(0xFFF2C230), Color(0xFF2A1F00));

    companion object {
        fun forKey(key: String): CrestTone = entries[(key.hashCode() and Int.MAX_VALUE) % entries.size]
    }
}
