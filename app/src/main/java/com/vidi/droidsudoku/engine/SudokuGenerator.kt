package com.vidi.droidsudoku.engine

import com.vidi.droidsudoku.data.Difficulty
import kotlin.random.Random

data class GeneratedPuzzle(val given: IntArray, val solution: IntArray)

/**
 * Generates uniquely-solvable puzzles: fill an empty grid completely via randomized backtracking,
 * then dig holes in shuffled cell order, keeping each removal only while the puzzle still has
 * exactly one solution (checked via [SudokuSolver.countSolutions] capped at 2).
 */
object SudokuGenerator {

    fun generate(difficulty: Difficulty, random: Random = Random.Default): GeneratedPuzzle =
        digPuzzle(difficulty.minClues, random)

    /** Challenge ladder entry point — clue count comes straight from the level ramp. */
    fun generateForLevel(level: Int, random: Random = Random.Default): GeneratedPuzzle =
        digPuzzle(Challenge.cluesForLevel(level), random)

    /** Digs a full solution down to (at most) [targetClues] cells while preserving a unique solution. */
    private fun digPuzzle(targetClues: Int, random: Random): GeneratedPuzzle {
        val solution = IntArray(81)
        fillGrid(solution, 0, random)

        val puzzle = solution.copyOf()
        val order = (0 until 81).toMutableList().apply { shuffle(random) }
        var clues = 81

        for (idx in order) {
            if (clues <= targetClues) break
            val backup = puzzle[idx]
            if (backup == 0) continue
            puzzle[idx] = 0

            val trial = puzzle.copyOf()
            val solutions = SudokuSolver.countSolutions(trial, cap = 2)
            if (solutions == 1) {
                clues--
            } else {
                puzzle[idx] = backup
            }
        }

        return GeneratedPuzzle(given = puzzle, solution = solution)
    }

    private fun fillGrid(grid: IntArray, index: Int, random: Random): Boolean {
        if (index == 81) return true
        if (grid[index] != 0) return fillGrid(grid, index + 1, random)

        val row = index / 9
        val col = index % 9
        val digits = (1..9).toMutableList().apply { shuffle(random) }
        for (digit in digits) {
            if (SudokuSolver.isLegal(grid, row, col, digit)) {
                grid[index] = digit
                if (fillGrid(grid, index + 1, random)) return true
                grid[index] = 0
            }
        }
        return false
    }
}
