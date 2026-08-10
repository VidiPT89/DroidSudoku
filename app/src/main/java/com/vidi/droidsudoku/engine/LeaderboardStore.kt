package com.vidi.droidsudoku.engine

import android.content.Context
import com.vidi.droidsudoku.data.Difficulty

data class RecordOutcome(val isNewBestTime: Boolean, val isNewBestHints: Boolean)

/** Per-difficulty best time / fewest hints used, persisted as a small JSON blob. */
class LeaderboardStore(context: Context) {
    private val prefs = context.getSharedPreferences("droidsudoku_leaderboard", Context.MODE_PRIVATE)

    fun bestTimeSeconds(difficulty: Difficulty): Int? =
        prefs.getInt(timeKey(difficulty), -1).takeIf { it >= 0 }

    fun bestHints(difficulty: Difficulty): Int? =
        prefs.getInt(hintsKey(difficulty), -1).takeIf { it >= 0 }

    fun recordCompletion(difficulty: Difficulty, elapsedSeconds: Int, hintsUsed: Int): RecordOutcome {
        val prevBestTime = bestTimeSeconds(difficulty)
        val prevBestHints = bestHints(difficulty)

        val isNewBestTime = prevBestTime == null || elapsedSeconds < prevBestTime
        val isNewBestHints = prevBestHints == null || hintsUsed < prevBestHints

        val editor = prefs.edit()
        if (isNewBestTime) editor.putInt(timeKey(difficulty), elapsedSeconds)
        if (isNewBestHints) editor.putInt(hintsKey(difficulty), hintsUsed)
        editor.apply()

        return RecordOutcome(isNewBestTime, isNewBestHints)
    }

    private fun timeKey(difficulty: Difficulty) = "best_time_${difficulty.name}"
    private fun hintsKey(difficulty: Difficulty) = "best_hints_${difficulty.name}"
}
