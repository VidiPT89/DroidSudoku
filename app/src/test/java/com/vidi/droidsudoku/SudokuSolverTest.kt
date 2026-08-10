package com.vidi.droidsudoku

import com.vidi.droidsudoku.engine.SudokuSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuSolverTest {

    private fun solvedGrid(): IntArray = intArrayOf(
        5, 3, 4, 6, 7, 8, 9, 1, 2,
        6, 7, 2, 1, 9, 5, 3, 4, 8,
        1, 9, 8, 3, 4, 2, 5, 6, 7,
        8, 5, 9, 7, 6, 1, 4, 2, 3,
        4, 2, 6, 8, 5, 3, 7, 9, 1,
        7, 1, 3, 9, 2, 4, 8, 5, 6,
        9, 6, 1, 5, 3, 7, 2, 8, 4,
        2, 8, 7, 4, 1, 9, 6, 3, 5,
        3, 4, 5, 2, 8, 6, 1, 7, 9
    )

    @Test
    fun `isLegal rejects a digit already present in the row`() {
        val grid = solvedGrid()
        grid[1] = 0
        assertFalse(SudokuSolver.isLegal(grid, 0, 1, 5))
    }

    @Test
    fun `isLegal rejects a digit already present in the column`() {
        val grid = solvedGrid()
        grid[9] = 0
        assertFalse(SudokuSolver.isLegal(grid, 1, 0, 5))
    }

    @Test
    fun `isLegal rejects a digit already present in the box`() {
        val grid = solvedGrid()
        grid[10] = 0
        assertFalse(SudokuSolver.isLegal(grid, 1, 1, 5))
    }

    @Test
    fun `isLegal accepts a digit with no conflicts`() {
        val grid = solvedGrid()
        grid[0] = 0
        assertTrue(SudokuSolver.isLegal(grid, 0, 0, 5))
    }

    @Test
    fun `countSolutions finds exactly one solution for a fully solved grid`() {
        assertEquals(1, SudokuSolver.countSolutions(solvedGrid(), cap = 2))
    }

    @Test
    fun `countSolutions finds multiple solutions for a near-empty grid`() {
        val empty = IntArray(81)
        assertEquals(2, SudokuSolver.countSolutions(empty, cap = 2))
    }

    @Test
    fun `findConflictIndices is empty for a valid solved grid`() {
        assertTrue(SudokuSolver.findConflictIndices(solvedGrid()).isEmpty())
    }

    @Test
    fun `findConflictIndices flags duplicate digits in the same row`() {
        val grid = solvedGrid()
        grid[1] = grid[0] // duplicate 5 in row 0
        val conflicts = SudokuSolver.findConflictIndices(grid)
        assertTrue(0 in conflicts)
        assertTrue(1 in conflicts)
    }

    @Test
    fun `findConflictIndices flags duplicate digits in the same column`() {
        val grid = solvedGrid()
        grid[9] = grid[0] // duplicate 5 in column 0
        val conflicts = SudokuSolver.findConflictIndices(grid)
        assertTrue(0 in conflicts)
        assertTrue(9 in conflicts)
    }

    @Test
    fun `findConflictIndices flags duplicate digits in the same box`() {
        val grid = solvedGrid()
        grid[10] = grid[0] // duplicate 5 in box 0
        val conflicts = SudokuSolver.findConflictIndices(grid)
        assertTrue(0 in conflicts)
        assertTrue(10 in conflicts)
    }
}
