package com.vidi.droidsudoku.engine

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists a single in-progress game as a JSON blob in SharedPreferences, so the player can
 * resume exactly where they left off ("Continue" on the main menu).
 */
class SaveStore(context: Context) {
    private val prefs = context.getSharedPreferences("droidsudoku_save", Context.MODE_PRIVATE)

    fun save(snapshot: SudokuSnapshot) {
        val json = JSONObject().apply {
            put("difficulty", snapshot.difficulty)
            put("given", JSONArray(snapshot.given.toList()))
            put("solution", JSONArray(snapshot.solution.toList()))
            put("values", JSONArray(snapshot.values.toList()))
            put("notes", JSONArray(snapshot.notes.map { JSONArray(it) }))
            put("selectedIndex", snapshot.selectedIndex ?: -1)
            put("notesMode", snapshot.notesMode)
            put("moves", snapshot.moves)
            put("hintsUsed", snapshot.hintsUsed)
            put("elapsedSeconds", snapshot.elapsedSeconds)
        }
        prefs.edit().putString("snapshot", json.toString()).apply()
    }

    fun hasSave(): Boolean = prefs.contains("snapshot")

    fun load(): SudokuSnapshot? {
        val raw = prefs.getString("snapshot", null) ?: return null
        return try {
            val json = JSONObject(raw)
            fun intArray(key: String): IntArray {
                val arr = json.getJSONArray(key)
                return IntArray(arr.length()) { arr.getInt(it) }
            }
            val notesArr = json.getJSONArray("notes")
            val notes = List(notesArr.length()) { i ->
                val inner = notesArr.getJSONArray(i)
                List(inner.length()) { j -> inner.getInt(j) }
            }
            val selected = json.getInt("selectedIndex").takeIf { it >= 0 }
            SudokuSnapshot(
                difficulty = json.getString("difficulty"),
                given = intArray("given"),
                solution = intArray("solution"),
                values = intArray("values"),
                notes = notes,
                selectedIndex = selected,
                notesMode = json.getBoolean("notesMode"),
                moves = json.getInt("moves"),
                hintsUsed = json.getInt("hintsUsed"),
                elapsedSeconds = json.getInt("elapsedSeconds")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun clear() {
        prefs.edit().remove("snapshot").apply()
    }
}
