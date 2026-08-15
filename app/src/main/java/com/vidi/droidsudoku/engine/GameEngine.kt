package com.vidi.droidsudoku.engine

import com.vidi.droidsudoku.data.Difficulty
import com.vidi.droidsudoku.data.SudokuCell
import kotlin.random.Random

sealed class SudokuResult {
    data class CellSelected(val index: Int) : SudokuResult()
    data class DigitEntered(val index: Int, val digit: Int) : SudokuResult()
    data class CellCleared(val index: Int) : SudokuResult()
    data class NoteToggled(val index: Int, val digit: Int) : SudokuResult()
    object Undone : SudokuResult()
    data class HintRevealed(val index: Int, val digit: Int) : SudokuResult()
    object HintExhausted : SudokuResult()
    object Won : SudokuResult()
    object Ignored : SudokuResult()
}

private data class UndoEntry(
    val index: Int,
    val prevValue: Int?,
    val prevNotes: Set<Int>,
    val wasHint: Boolean = false
)

/**
 * Owns the full mutable game state for one puzzle: cells, selection, notes-mode, move/hint
 * counters and elapsed time. Every public mutator returns a [SudokuResult] describing what
 * happened so the UI layer can react (sound, toast, save, modal) without re-deriving state.
 */
class GameEngine private constructor(
    val difficulty: Difficulty,
    private val givenMask: BooleanArray,
    private val solution: IntArray,
    initialValues: IntArray,
    initialNotes: List<Set<Int>>,
    var selectedIndex: Int? = null,
    var notesMode: Boolean = false,
    var moves: Int = 0,
    var hintsUsed: Int = 0,
    var wrongMoves: Int = 0,
    val challengeLevel: Int? = null,
    var elapsedSeconds: Int = 0
) {
    val isChallenge: Boolean get() = challengeLevel != null
    private val values = initialValues.copyOf()
    private val notes = MutableList(81) { initialNotes[it].toMutableSet() }
    private val undoStack = ArrayDeque<UndoEntry>()

    val cells: List<SudokuCell>
        get() {
            val conflicts = SudokuSolver.findConflictIndices(values)
            return (0 until 81).map { idx ->
                SudokuCell(
                    row = idx / 9,
                    col = idx % 9,
                    isGiven = givenMask[idx],
                    value = values[idx].takeIf { it != 0 },
                    notes = notes[idx].toSet(),
                    isConflict = idx in conflicts
                )
            }
        }

    val isWon: Boolean
        get() {
            if (values.any { it == 0 }) return false
            return SudokuSolver.findConflictIndices(values).isEmpty()
        }

    val maxHints: Int get() = if (isChallenge) Challenge.maxHintsForLevel(challengeLevel!!) else difficulty.maxHints
    val hintsRemaining: Int get() = maxHints - hintsUsed
    val canUndo: Boolean get() = undoStack.isNotEmpty()

    fun selectCell(index: Int): SudokuResult {
        if (index !in 0 until 81) return SudokuResult.Ignored
        selectedIndex = index
        return SudokuResult.CellSelected(index)
    }

    fun enterDigit(digit: Int): SudokuResult {
        val index = selectedIndex ?: return SudokuResult.Ignored
        if (givenMask[index]) return SudokuResult.Ignored
        if (digit !in 1..9) return SudokuResult.Ignored

        if (notesMode) {
            val had = digit in notes[index]
            pushUndo(index)
            if (had) notes[index].remove(digit) else notes[index].add(digit)
            moves++
            return SudokuResult.NoteToggled(index, digit)
        }

        if (values[index] == digit) return SudokuResult.Ignored
        pushUndo(index)
        values[index] = digit
        notes[index].clear()
        moves++
        if (digit != solution[index]) wrongMoves++
        return if (isWon) SudokuResult.Won else SudokuResult.DigitEntered(index, digit)
    }

    fun clearCell(): SudokuResult {
        val index = selectedIndex ?: return SudokuResult.Ignored
        if (givenMask[index]) return SudokuResult.Ignored
        if (values[index] == 0 && notes[index].isEmpty()) return SudokuResult.Ignored
        pushUndo(index)
        values[index] = 0
        notes[index].clear()
        moves++
        return SudokuResult.CellCleared(index)
    }

    fun undo(): SudokuResult {
        val entry = undoStack.removeLastOrNull() ?: return SudokuResult.Ignored
        values[entry.index] = entry.prevValue ?: 0
        notes[entry.index] = entry.prevNotes.toMutableSet()
        selectedIndex = entry.index
        if (entry.wasHint && hintsUsed > 0) hintsUsed--
        return SudokuResult.Undone
    }

    fun toggleNotesMode() {
        notesMode = !notesMode
    }

    fun hint(random: Random = Random.Default): SudokuResult {
        if (hintsUsed >= maxHints) return SudokuResult.HintExhausted

        val selected = selectedIndex
        val target = if (selected != null && !givenMask[selected] && values[selected] != solution[selected]) {
            selected
        } else {
            val candidates = (0 until 81).filter { !givenMask[it] && values[it] != solution[it] }
            if (candidates.isEmpty()) return SudokuResult.HintExhausted
            candidates[random.nextInt(candidates.size)]
        }

        pushUndo(target, wasHint = true)
        values[target] = solution[target]
        notes[target].clear()
        hintsUsed++
        moves++
        selectedIndex = target
        return if (isWon) SudokuResult.Won else SudokuResult.HintRevealed(target, solution[target])
    }

    private fun pushUndo(index: Int, wasHint: Boolean = false) {
        undoStack.addLast(UndoEntry(index, values[index].takeIf { it != 0 }, notes[index].toSet(), wasHint))
        if (undoStack.size > 200) undoStack.removeFirst()
    }

    fun toSnapshot(): SudokuSnapshot = SudokuSnapshot(
        difficulty = difficulty.name,
        given = givenMask.map { if (it) 1 else 0 }.toIntArray(),
        solution = solution.copyOf(),
        values = values.copyOf(),
        notes = notes.map { it.toList() },
        selectedIndex = selectedIndex,
        notesMode = notesMode,
        moves = moves,
        hintsUsed = hintsUsed,
        wrongMoves = wrongMoves,
        challengeLevel = challengeLevel,
        elapsedSeconds = elapsedSeconds
    )

    companion object {
        fun newGame(difficulty: Difficulty, random: Random = Random.Default): GameEngine {
            val generated = SudokuGenerator.generate(difficulty, random)
            val given = BooleanArray(81) { generated.given[it] != 0 }
            return GameEngine(
                difficulty = difficulty,
                givenMask = given,
                solution = generated.solution,
                initialValues = generated.given,
                initialNotes = List(81) { emptySet() }
            )
        }

        fun newChallenge(level: Int, random: Random = Random.Default): GameEngine {
            val generated = SudokuGenerator.generateForLevel(level, random)
            val given = BooleanArray(81) { generated.given[it] != 0 }
            return GameEngine(
                difficulty = Challenge.bandForLevel(level),
                givenMask = given,
                solution = generated.solution,
                initialValues = generated.given,
                initialNotes = List(81) { emptySet() },
                challengeLevel = level
            )
        }

        fun fromSnapshot(snapshot: SudokuSnapshot): GameEngine {
            val difficulty = Difficulty.valueOf(snapshot.difficulty)
            val given = BooleanArray(81) { snapshot.given[it] != 0 }
            return GameEngine(
                difficulty = difficulty,
                givenMask = given,
                solution = snapshot.solution,
                initialValues = snapshot.values,
                initialNotes = snapshot.notes.map { it.toSet() },
                selectedIndex = snapshot.selectedIndex,
                notesMode = snapshot.notesMode,
                moves = snapshot.moves,
                hintsUsed = snapshot.hintsUsed,
                wrongMoves = snapshot.wrongMoves,
                challengeLevel = snapshot.challengeLevel,
                elapsedSeconds = snapshot.elapsedSeconds
            )
        }
    }
}

data class SudokuSnapshot(
    val difficulty: String,
    val given: IntArray,
    val solution: IntArray,
    val values: IntArray,
    val notes: List<List<Int>>,
    val selectedIndex: Int?,
    val notesMode: Boolean,
    val moves: Int,
    val hintsUsed: Int,
    val wrongMoves: Int = 0,
    val challengeLevel: Int? = null,
    val elapsedSeconds: Int
)
