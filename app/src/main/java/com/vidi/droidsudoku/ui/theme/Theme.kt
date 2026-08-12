package com.vidi.droidsudoku.ui.theme

import androidx.compose.ui.graphics.Color

/** ividi.dev-matched dark/amber palette, identical across all Vidi apps, plus Sudoku-specific
 * tokens for cell states (given/user digits, selection, peer highlight, conflicts, notes). */
object Theme {
    val bg = Color(0xFF0A0A0F)
    val bgPanel = Color(0xFF0D0D18)
    val bgPanel2 = Color(0xFF12121F)

    val text = Color(0xFFE2E8F0)
    val textDim = Color(0xFF94A3B8)
    val textFaint = Color(0xFF5B6474)

    val accent = Color(0xFFF99C00)
    val accentLight = Color(0xFFFCBB00)
    val accentDark = Color(0xFFDD7400)

    val danger = Color(0xFFEF4444)
    val ok = Color(0xFF22C55E)

    val border = Color(0xFFE2E8F0).copy(alpha = 0.10f)
    val borderStrong = Color(0xFFE2E8F0).copy(alpha = 0.18f)

    // Sudoku-specific
    val givenDigit = text
    val userDigit = accentLight
    val selectedCellBg = accent.copy(alpha = 0.20f)
    val peerHighlightBg = accent.copy(alpha = 0.09f)
    val sameNumberHighlightBg = accentLight.copy(alpha = 0.13f)
    val conflictBg = danger.copy(alpha = 0.16f)
    val conflictText = danger
    val noteText = textFaint
}
