package com.vidi.droidsudoku.data

/**
 * A single cell in the 9x9 grid. [row]/[col] are 0-based board coordinates; [index] = row*9+col
 * is the flat position used throughout the engine. Given cells are fixed at generation time and
 * can never be edited or noted. [notes] holds candidate pencil-marks (1-9) entered by the player.
 */
data class SudokuCell(
    val row: Int,
    val col: Int,
    val isGiven: Boolean = false,
    val value: Int? = null,
    val notes: Set<Int> = emptySet(),
    val isConflict: Boolean = false
) {
    val index: Int get() = row * 9 + col
    val boxIndex: Int get() = (row / 3) * 3 + (col / 3)
}

enum class Difficulty(val minClues: Int, val maxClues: Int, val maxHints: Int) {
    EASY(40, 45, 3),
    MEDIUM(32, 36, 4),
    HARD(26, 30, 5),

    // Used only as the top band of Challenge mode; never offered in the classic difficulty picker.
    EXPERT(23, 25, 5)
}
