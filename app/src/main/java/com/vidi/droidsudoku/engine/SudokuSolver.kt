package com.vidi.droidsudoku.engine

/**
 * Pure backtracking utilities shared by the generator (uniqueness checks) and, at generation
 * time, the full-grid solve. The board is represented as a flat IntArray(81) of digits 1-9,
 * with 0 meaning empty — this is the fastest shape for the tight recursive loops below.
 */
object SudokuSolver {

    fun isLegal(grid: IntArray, row: Int, col: Int, digit: Int): Boolean {
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (i in 0 until 9) {
            if (grid[row * 9 + i] == digit) return false
            if (grid[i * 9 + col] == digit) return false
            val br = boxRow + i / 3
            val bc = boxCol + i % 3
            if (grid[br * 9 + bc] == digit) return false
        }
        return true
    }

    /** Finds the empty cell with the fewest legal candidates (MRV heuristic). Returns
     * Triple(index, candidateList, hasZeroCandidates) or null if the grid is already full. */
    private fun findMrvCell(grid: IntArray): Pair<Int, List<Int>>? {
        var bestIndex = -1
        var bestCandidates: List<Int>? = null
        for (idx in 0 until 81) {
            if (grid[idx] != 0) continue
            val row = idx / 9
            val col = idx % 9
            val candidates = (1..9).filter { isLegal(grid, row, col, it) }
            if (candidates.isEmpty()) return idx to emptyList()
            if (bestCandidates == null || candidates.size < bestCandidates.size) {
                bestIndex = idx
                bestCandidates = candidates
                if (candidates.size == 1) break
            }
        }
        return if (bestIndex == -1) null else bestIndex to bestCandidates!!
    }

    /** Counts solutions up to [cap], stopping early once reached. Used both to verify a puzzle
     * has exactly one solution (cap = 2) and, potentially, deeper solving needs later. */
    fun countSolutions(grid: IntArray, cap: Int = 2): Int {
        val cell = findMrvCell(grid) ?: return 1
        val (idx, candidates) = cell
        if (candidates.isEmpty()) return 0
        var found = 0
        for (digit in candidates) {
            grid[idx] = digit
            found += countSolutions(grid, cap - found)
            grid[idx] = 0
            if (found >= cap) break
        }
        return found
    }

    /** Recomputes conflict flags for every occupied cell: any digit repeated within its row,
     * column, or 3x3 box marks all cells sharing that digit in that unit as conflicting. */
    fun findConflictIndices(values: IntArray): Set<Int> {
        val conflicts = mutableSetOf<Int>()

        fun scanUnit(indices: List<Int>) {
            val seen = HashMap<Int, MutableList<Int>>()
            for (idx in indices) {
                val v = values[idx]
                if (v == 0) continue
                seen.getOrPut(v) { mutableListOf() }.add(idx)
            }
            for ((_, idxs) in seen) {
                if (idxs.size > 1) conflicts.addAll(idxs)
            }
        }

        for (row in 0 until 9) scanUnit((0 until 9).map { row * 9 + it })
        for (col in 0 until 9) scanUnit((0 until 9).map { it * 9 + col })
        for (boxRow in 0 until 3) for (boxCol in 0 until 3) {
            val idxs = mutableListOf<Int>()
            for (r in 0 until 3) for (c in 0 until 3) {
                idxs.add((boxRow * 3 + r) * 9 + (boxCol * 3 + c))
            }
            scanUnit(idxs)
        }
        return conflicts
    }
}
