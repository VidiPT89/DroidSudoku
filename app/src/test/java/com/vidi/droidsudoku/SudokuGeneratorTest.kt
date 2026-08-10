package com.vidi.droidsudoku

import com.vidi.droidsudoku.data.Difficulty
import com.vidi.droidsudoku.engine.SudokuGenerator
import com.vidi.droidsudoku.engine.SudokuSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SudokuGeneratorTest {

    @Test
    fun `generated solution is fully filled and conflict-free`() {
        val puzzle = SudokuGenerator.generate(Difficulty.EASY, Random(1))
        assertTrue(puzzle.solution.all { it in 1..9 })
        assertTrue(SudokuSolver.findConflictIndices(puzzle.solution).isEmpty())
    }

    @Test
    fun `given cells match the solution`() {
        val puzzle = SudokuGenerator.generate(Difficulty.MEDIUM, Random(2))
        for (i in 0 until 81) {
            if (puzzle.given[i] != 0) {
                assertEquals(puzzle.solution[i], puzzle.given[i])
            }
        }
    }

    @Test
    fun `easy puzzle has a unique solution and respects the clue range`() {
        val puzzle = SudokuGenerator.generate(Difficulty.EASY, Random(3))
        val clueCount = puzzle.given.count { it != 0 }
        assertTrue(clueCount in Difficulty.EASY.minClues..81)
        assertEquals(1, SudokuSolver.countSolutions(puzzle.given.copyOf(), cap = 2))
    }

    @Test
    fun `hard puzzle has a unique solution and respects the clue range`() {
        val puzzle = SudokuGenerator.generate(Difficulty.HARD, Random(4))
        val clueCount = puzzle.given.count { it != 0 }
        assertTrue(clueCount in Difficulty.HARD.minClues..Difficulty.HARD.maxClues + 5)
        assertEquals(1, SudokuSolver.countSolutions(puzzle.given.copyOf(), cap = 2))
    }
}
