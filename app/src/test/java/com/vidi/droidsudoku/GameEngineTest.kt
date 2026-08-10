package com.vidi.droidsudoku

import com.vidi.droidsudoku.data.Difficulty
import com.vidi.droidsudoku.engine.GameEngine
import com.vidi.droidsudoku.engine.SudokuResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    private fun newEngine(difficulty: Difficulty = Difficulty.EASY, seed: Int = 42) =
        GameEngine.newGame(difficulty, Random(seed))

    @Test
    fun `entering a digit on an empty non-given cell commits the value`() {
        val engine = newEngine()
        val target = engine.cells.indexOfFirst { !it.isGiven }
        engine.selectCell(target)
        val result = engine.enterDigit(5)
        assertTrue(result is SudokuResult.DigitEntered || result is SudokuResult.Won)
        assertEquals(5, engine.cells[target].value)
    }

    @Test
    fun `entering a digit on a given cell is ignored`() {
        val engine = newEngine()
        val target = engine.cells.indexOfFirst { it.isGiven }
        val originalValue = engine.cells[target].value
        engine.selectCell(target)
        val result = engine.enterDigit(if (originalValue == 9) 1 else 9)
        assertEquals(SudokuResult.Ignored, result)
        assertEquals(originalValue, engine.cells[target].value)
    }

    @Test
    fun `undo reverts the last digit entry`() {
        val engine = newEngine()
        val target = engine.cells.indexOfFirst { !it.isGiven }
        engine.selectCell(target)
        engine.enterDigit(7)
        assertEquals(7, engine.cells[target].value)
        engine.undo()
        assertEquals(null, engine.cells[target].value)
    }

    @Test
    fun `toggling a note twice removes it`() {
        val engine = newEngine()
        val target = engine.cells.indexOfFirst { !it.isGiven }
        engine.selectCell(target)
        engine.toggleNotesMode()
        engine.enterDigit(3)
        assertTrue(3 in engine.cells[target].notes)
        engine.enterDigit(3)
        assertFalse(3 in engine.cells[target].notes)
    }

    @Test
    fun `entering a value clears any notes on that cell`() {
        val engine = newEngine()
        val target = engine.cells.indexOfFirst { !it.isGiven }
        engine.selectCell(target)
        engine.toggleNotesMode()
        engine.enterDigit(2)
        engine.enterDigit(4)
        engine.toggleNotesMode()
        engine.enterDigit(6)
        assertTrue(engine.cells[target].notes.isEmpty())
        assertEquals(6, engine.cells[target].value)
    }

    @Test
    fun `duplicate digits in the same row are flagged as conflicts`() {
        val engine = newEngine()
        val rowZeroEmpty = engine.cells.filter { it.row == 0 && !it.isGiven }
        assertTrue(rowZeroEmpty.size >= 2)
        val a = rowZeroEmpty[0].index
        val b = rowZeroEmpty[1].index

        engine.selectCell(a)
        engine.enterDigit(1)
        engine.selectCell(b)
        engine.enterDigit(1)

        assertTrue(engine.cells[a].isConflict)
        assertTrue(engine.cells[b].isConflict)
    }

    @Test
    fun `hint fills a cell using the cached solution and increments hint count`() {
        val engine = newEngine()
        val before = engine.hintsUsed
        engine.hint(Random(1))
        assertEquals(before + 1, engine.hintsUsed)
    }

    @Test
    fun `hint is exhausted once the per-difficulty cap is reached`() {
        val engine = newEngine(Difficulty.EASY)
        repeat(Difficulty.EASY.maxHints) { engine.hint(Random(it)) }
        val result = engine.hint(Random(99))
        assertEquals(SudokuResult.HintExhausted, result)
    }

    @Test
    fun `completing the last empty cell with the correct digit wins the game`() {
        // Reveal every non-given cell via hint() to learn the solution digits through the
        // public API (undoing each reveal immediately so the cap is never actually spent),
        // then replay them all via enterDigit and confirm the final entry reports Won.
        val engine = newEngine()
        val nonGiven = engine.cells.filter { !it.isGiven }.map { it.index }

        val solutionDigits = nonGiven.associateWith { idx ->
            engine.selectCell(idx)
            engine.hint(Random(idx))
            val digit = engine.cells[idx].value!!
            engine.undo()
            digit
        }

        var result: SudokuResult = SudokuResult.Ignored
        for (idx in nonGiven) {
            engine.selectCell(idx)
            result = engine.enterDigit(solutionDigits.getValue(idx))
        }

        assertTrue(result is SudokuResult.Won)
        assertTrue(engine.isWon)
    }
}
