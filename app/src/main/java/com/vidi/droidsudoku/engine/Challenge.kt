package com.vidi.droidsudoku.engine

import android.content.Context
import com.vidi.droidsudoku.data.Difficulty
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Challenge mode: an endless ladder of generated levels.
 *
 * Level 1 opens a shade easier than the classic EASY preset and every level shaves a fraction
 * of a clue off the grid, bottoming out at an expert floor below the classic HARD preset.
 * Because the ramp is a pure function of the level number there is no level content to author
 * or ship — the ladder simply never ends.
 *
 * Stars rate a finished level on mistakes and hints only, never on time: a player who thinks
 * slowly is not a worse player.
 */
object Challenge {

    /** Ramp knobs — tune these, not the formula. */
    const val START_CLUES = 42
    const val MIN_CLUES = 23
    private const val CLUES_PER_LEVEL = 0.55

    /** How much of the ladder the level picker keeps on screen at once. */
    const val LEVELS_BEHIND = 30
    const val LEVELS_AHEAD = 5

    /** Target clue count for a level, clamped so the ladder plateaus at the expert floor. */
    fun cluesForLevel(level: Int): Int {
        val n = max(1, level)
        val target = (START_CLUES - (n - 1) * CLUES_PER_LEVEL).roundToInt()
        return min(START_CLUES, max(MIN_CLUES, target))
    }

    /** Maps a level onto the classic difficulty vocabulary, for labels and hint budgets. */
    fun bandForLevel(level: Int): Difficulty {
        val clues = cluesForLevel(level)
        return when {
            clues >= 38 -> Difficulty.EASY
            clues >= 32 -> Difficulty.MEDIUM
            clues >= 26 -> Difficulty.HARD
            else -> Difficulty.EXPERT
        }
    }

    fun maxHintsForLevel(level: Int): Int = bandForLevel(level).maxHints

    /** 3 = flawless, 2 = a few slips, 1 = finished. A mistake and a hint cost the same. */
    fun starsFor(wrongMoves: Int, hintsUsed: Int): Int {
        val penalty = max(0, wrongMoves) + max(0, hintsUsed)
        return when {
            penalty == 0 -> 3
            penalty <= 3 -> 2
            else -> 1
        }
    }

    /**
     * Pure progression rule, kept free of storage so it can be unit-tested: finishing a level
     * keeps the player's best result and only ever pushes the frontier forward.
     */
    fun applyWin(
        progress: ChallengeProgress,
        level: Int,
        elapsedSeconds: Int,
        stars: Int
    ): Pair<ChallengeProgress, ChallengeOutcome> {
        val prevStars = progress.stars[level] ?: 0
        val prevBest = progress.bestTime[level]

        val improvedStars = stars > prevStars
        val isNewBestTime = prevBest == null || elapsedSeconds < prevBest
        val unlockedNext = level >= progress.highestUnlocked

        val updated = progress.copy(
            highestUnlocked = if (unlockedNext) level + 1 else progress.highestUnlocked,
            stars = if (improvedStars) progress.stars + (level to stars) else progress.stars,
            bestTime = if (isNewBestTime) progress.bestTime + (level to elapsedSeconds) else progress.bestTime
        )

        val outcome = ChallengeOutcome(
            improvedStars = improvedStars,
            isNewBestTime = isNewBestTime,
            unlockedNext = unlockedNext,
            bestStars = max(stars, prevStars),
            bestTimeSeconds = updated.bestTime[level] ?: elapsedSeconds
        )
        return updated to outcome
    }
}

data class ChallengeProgress(
    val highestUnlocked: Int = 1,
    val stars: Map<Int, Int> = emptyMap(),
    val bestTime: Map<Int, Int> = emptyMap()
) {
    val totalStars: Int get() = stars.values.sum()
    fun starsAt(level: Int): Int = stars[level] ?: 0
    fun isUnlocked(level: Int): Boolean = level <= highestUnlocked
}

data class ChallengeOutcome(
    val improvedStars: Boolean,
    val isNewBestTime: Boolean,
    val unlockedNext: Boolean,
    val bestStars: Int,
    val bestTimeSeconds: Int
)

/** Persists challenge progress as a small JSON blob in SharedPreferences. */
class ChallengeStore(context: Context) {
    private val prefs = context.getSharedPreferences("droidsudoku_challenges", Context.MODE_PRIVATE)

    fun load(): ChallengeProgress {
        val raw = prefs.getString("progress", null) ?: return ChallengeProgress()
        return try {
            val json = JSONObject(raw)
            ChallengeProgress(
                highestUnlocked = max(1, json.optInt("highestUnlocked", 1)),
                stars = readMap(json.optJSONObject("stars")),
                bestTime = readMap(json.optJSONObject("bestTime"))
            )
        } catch (e: Exception) {
            ChallengeProgress()
        }
    }

    fun save(progress: ChallengeProgress) {
        val json = JSONObject().apply {
            put("highestUnlocked", progress.highestUnlocked)
            put("stars", JSONObject().apply { progress.stars.forEach { (k, v) -> put(k.toString(), v) } })
            put("bestTime", JSONObject().apply { progress.bestTime.forEach { (k, v) -> put(k.toString(), v) } })
        }
        prefs.edit().putString("progress", json.toString()).apply()
    }

    fun recordWin(level: Int, elapsedSeconds: Int, stars: Int): ChallengeOutcome {
        val (updated, outcome) = Challenge.applyWin(load(), level, elapsedSeconds, stars)
        save(updated)
        return outcome
    }

    private fun readMap(obj: JSONObject?): Map<Int, Int> {
        if (obj == null) return emptyMap()
        val out = mutableMapOf<Int, Int>()
        obj.keys().forEach { key ->
            key.toIntOrNull()?.let { out[it] = obj.optInt(key, 0) }
        }
        return out
    }
}
