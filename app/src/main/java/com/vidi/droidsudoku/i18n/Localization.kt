package com.vidi.droidsudoku.i18n

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Lang { PT, EN }

/** Tiny map-backed localizer, defaulting to Portuguese and persisted via SharedPreferences.
 * Backed by mutableStateOf so Compose recomposes automatically when the language toggles. */
class Localization(private val context: Context) {
    private val prefs = context.getSharedPreferences("droidsudoku_prefs", Context.MODE_PRIVATE)

    var lang by mutableStateOf(
        Lang.entries.firstOrNull { it.name == prefs.getString("lang", null) } ?: Lang.PT
    )
        private set

    fun toggle() {
        lang = if (lang == Lang.PT) Lang.EN else Lang.PT
        prefs.edit().putString("lang", lang.name).apply()
    }

    fun t(key: String): String = strings[lang]?.get(key) ?: key

    companion object {
        private val strings: Map<Lang, Map<String, String>> = mapOf(
            Lang.PT to mapOf(
                "tapToContinue" to "Toque para continuar",
                "developedBy" to "Desenvolvido por",

                "menuTag" to "PUZZLE DE LÓGICA",
                "menuSubtitle" to "Preenche a grelha 9x9. Sem repetições em linhas, colunas ou blocos.",
                "sudokuTag" to "SUDOKU",
                "play" to "Jogar",
                "continueGame" to "Continuar",
                "howToPlay" to "Como Jogar",

                "difficultyLabel" to "Dificuldade",
                "easy" to "Fácil",
                "medium" to "Médio",
                "hard" to "Difícil",

                "notes" to "Notas",
                "erase" to "Apagar",
                "newGame" to "Novo Jogo",
                "hint" to "Dica",
                "undo" to "Anular",
                "restart" to "Reiniciar",
                "time" to "Tempo",
                "moves" to "Jogadas",
                "hintsLeft" to "Dicas",
                "noHintsLeft" to "Sem mais dicas disponíveis.",
                "nothingToUndo" to "Nada para anular.",

                "winTitle" to "Grelha Completa!",
                "winSubtitle" to "Resolveste o puzzle com sucesso.",
                "playAgain" to "Jogar Novamente",
                "backToMenu" to "Voltar ao Menu",
                "bestTime" to "Melhor Tempo",
                "bestHints" to "Menos Dicas",
                "newRecordTime" to "Novo recorde!",
                "newRecordHints" to "Novo recorde!",

                "confirmRestartTitle" to "Reiniciar Puzzle?",
                "confirmRestartBody" to "Vais perder o progresso atual neste puzzle.",
                "confirm" to "Confirmar",
                "cancel" to "Cancelar",

                "htpTitle" to "Como Jogar",
                "htpIntro" to "O objetivo é preencher a grelha 9x9 com os números de 1 a 9.",
                "htpRuleTitle" to "A Regra",
                "htpRuleBody" to "Cada linha, cada coluna e cada bloco 3x3 tem de conter todos os números de 1 a 9, sem repetições.",
                "htpGivenTitle" to "Números Fixos",
                "htpGivenBody" to "Os números já preenchidos no início são fixos e não podem ser alterados. Os restantes são preenchidos por ti.",
                "htpConflictTitle" to "Conflitos",
                "htpConflictBody" to "Se colocares um número que já existe na mesma linha, coluna ou bloco, essas células ficam destacadas a vermelho.",
                "htpNotesTitle" to "Notas",
                "htpNotesBody" to "Ativa o modo notas para marcar candidatos possíveis numa célula, em vez de preencher um valor definitivo.",
                "htpToolsTitle" to "Ferramentas",
                "htpHintTool" to "Dica: revela o valor correto de uma célula. Limitado por jogo.",
                "htpUndoTool" to "Anular: desfaz a tua última jogada.",
                "htpCloseButton" to "Entendido"
            ),
            Lang.EN to mapOf(
                "tapToContinue" to "Tap to continue",
                "developedBy" to "Developed by",

                "menuTag" to "LOGIC PUZZLE",
                "menuSubtitle" to "Fill the 9x9 grid. No repeats in any row, column or box.",
                "sudokuTag" to "SUDOKU",
                "play" to "Play",
                "continueGame" to "Continue",
                "howToPlay" to "How to Play",

                "difficultyLabel" to "Difficulty",
                "easy" to "Easy",
                "medium" to "Medium",
                "hard" to "Hard",

                "notes" to "Notes",
                "erase" to "Erase",
                "newGame" to "New Game",
                "hint" to "Hint",
                "undo" to "Undo",
                "restart" to "Restart",
                "time" to "Time",
                "moves" to "Moves",
                "hintsLeft" to "Hints",
                "noHintsLeft" to "No more hints available.",
                "nothingToUndo" to "Nothing to undo.",

                "winTitle" to "Grid Complete!",
                "winSubtitle" to "You solved the puzzle.",
                "playAgain" to "Play Again",
                "backToMenu" to "Back to Menu",
                "bestTime" to "Best Time",
                "bestHints" to "Fewest Hints",
                "newRecordTime" to "New record!",
                "newRecordHints" to "New record!",

                "confirmRestartTitle" to "Restart Puzzle?",
                "confirmRestartBody" to "You'll lose your current progress on this puzzle.",
                "confirm" to "Confirm",
                "cancel" to "Cancel",

                "htpTitle" to "How to Play",
                "htpIntro" to "The goal is to fill the 9x9 grid with the digits 1 through 9.",
                "htpRuleTitle" to "The Rule",
                "htpRuleBody" to "Every row, every column and every 3x3 box must contain each digit from 1 to 9 exactly once.",
                "htpGivenTitle" to "Given Numbers",
                "htpGivenBody" to "Numbers already filled in at the start are fixed and cannot be changed. The rest are yours to fill.",
                "htpConflictTitle" to "Conflicts",
                "htpConflictBody" to "If you place a number that already exists in the same row, column or box, those cells are highlighted in red.",
                "htpNotesTitle" to "Notes",
                "htpNotesBody" to "Turn on notes mode to pencil in possible candidates for a cell, instead of committing a final value.",
                "htpToolsTitle" to "Tools",
                "htpHintTool" to "Hint: reveals the correct value for a cell. Limited per game.",
                "htpUndoTool" to "Undo: reverts your last move.",
                "htpCloseButton" to "Got It"
            )
        )
    }
}
