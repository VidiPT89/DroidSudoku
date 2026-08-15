package com.vidi.droidsudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidi.droidsudoku.engine.Challenge
import com.vidi.droidsudoku.engine.GameEngine
import com.vidi.droidsudoku.engine.LeaderboardStore
import com.vidi.droidsudoku.engine.RecordOutcome
import com.vidi.droidsudoku.engine.SaveStore
import com.vidi.droidsudoku.engine.SudokuResult
import com.vidi.droidsudoku.i18n.Localization
import com.vidi.droidsudoku.ui.sound.SoundFx
import com.vidi.droidsudoku.ui.theme.Theme
import kotlinx.coroutines.delay

private enum class ModalKind { NONE, WIN, CONFIRM_RESTART }

@Composable
fun GameScreen(
    engine: GameEngine,
    loc: Localization,
    onExit: () -> Unit,
    onNewGame: () -> Unit,
    onNextChallenge: () -> Unit = {},
    onChallengeLevels: () -> Unit = {},
    onChallengeWin: (level: Int, stars: Int, elapsedSeconds: Int) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val saveStore = remember { SaveStore(context) }
    val leaderboardStore = remember { LeaderboardStore(context) }
    val soundFx = remember { SoundFx() }
    DisposableEffect(Unit) { onDispose { soundFx.release() } }

    var version by remember { mutableStateOf(0) }
    var modal by remember { mutableStateOf(ModalKind.NONE) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val shakeTokens = remember { mutableStateMapOf<Int, Int>() }
    var recordOutcome by remember { mutableStateOf(RecordOutcome(false, false)) }
    var running by remember { mutableStateOf(true) }

    LaunchedEffect(running, modal) {
        while (running && modal == ModalKind.NONE) {
            delay(1000)
            engine.elapsedSeconds++
            version++
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(1800)
            toastMessage = null
        }
    }

    fun persist() {
        saveStore.save(engine.toSnapshot())
    }

    fun handleResult(result: SudokuResult) {
        when (result) {
            is SudokuResult.DigitEntered -> {
                soundFx.digitEnter()
                version++
                persist()
                val cell = engine.cells[result.index]
                if (cell.isConflict) {
                    soundFx.conflict()
                    shakeTokens[result.index] = (shakeTokens[result.index] ?: 0) + 1
                }
            }
            is SudokuResult.NoteToggled -> {
                soundFx.digitEnter()
                version++
                persist()
            }
            is SudokuResult.CellCleared -> {
                version++
                persist()
            }
            SudokuResult.Undone -> {
                version++
                persist()
            }
            is SudokuResult.HintRevealed -> {
                soundFx.hint()
                version++
                persist()
            }
            SudokuResult.HintExhausted -> {
                toastMessage = loc.t("noHintsLeft")
            }
            SudokuResult.Won -> {
                soundFx.win()
                running = false
                recordOutcome = leaderboardStore.recordCompletion(
                    engine.difficulty, engine.elapsedSeconds, engine.hintsUsed
                )
                saveStore.clear()
                modal = ModalKind.WIN
            }
            is SudokuResult.CellSelected -> version++
            SudokuResult.Ignored -> {}
        }
    }

    fun handleTap(index: Int) {
        handleResult(engine.selectCell(index))
    }

    fun handleDigit(digit: Int) {
        handleResult(engine.enterDigit(digit))
    }

    fun handleErase() {
        handleResult(engine.clearCell())
    }

    fun handleUndo() {
        if (!engine.canUndo) {
            toastMessage = loc.t("nothingToUndo")
            return
        }
        handleResult(engine.undo())
    }

    fun handleHint() {
        handleResult(engine.hint())
    }

    fun performRestart() {
        modal = ModalKind.NONE
        onNewGame()
    }

    val cells = engine.cells
    val remainingCounts = IntArray(9) { d ->
        9 - cells.count { it.value == d + 1 }
    }

    Box(Modifier.fillMaxSize()) {
        BackgroundGlow()

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            GameHeader(
                loc = loc,
                elapsedSeconds = engine.elapsedSeconds,
                moves = engine.moves,
                hintsRemaining = engine.hintsRemaining,
                onBack = { persist(); onExit() },
                onRestart = { modal = ModalKind.CONFIRM_RESTART }
            )

            Spacer(Modifier.height(16.dp))
            SudokuGridView(
                cells = cells,
                selectedIndex = engine.selectedIndex,
                shakeTokens = shakeTokens,
                onCellTap = { handleTap(it) }
            )

            Spacer(Modifier.height(18.dp))
            ActionRow(
                loc = loc,
                notesMode = engine.notesMode,
                hintsRemaining = engine.hintsRemaining,
                canUndo = engine.canUndo,
                onToggleNotes = { engine.toggleNotesMode(); version++ },
                onUndo = { handleUndo() },
                onHint = { handleHint() }
            )

            Spacer(Modifier.height(14.dp))
            NumberPad(
                remainingCounts = remainingCounts,
                notesMode = engine.notesMode,
                onDigit = { handleDigit(it) },
                onErase = { handleErase() }
            )
        }

        toastMessage?.let { ToastView(it) }

        if (modal == ModalKind.WIN) {
            WinModal(
                loc = loc,
                elapsedSeconds = engine.elapsedSeconds,
                hintsUsed = engine.hintsUsed,
                bestTimeSeconds = leaderboardStore.bestTimeSeconds(engine.difficulty),
                bestHints = leaderboardStore.bestHints(engine.difficulty),
                recordOutcome = recordOutcome,
                onPlayAgain = { performRestart() },
                onBackToMenu = { modal = ModalKind.NONE; onExit() }
            )
        } else if (modal == ModalKind.CONFIRM_RESTART) {
            ConfirmModal(
                title = loc.t("confirmRestartTitle"),
                body = loc.t("confirmRestartBody"),
                confirmLabel = loc.t("confirm"),
                cancelLabel = loc.t("cancel"),
                onConfirm = { performRestart() },
                onCancel = { modal = ModalKind.NONE }
            )
        }
    }
}

@Composable
private fun GameHeader(
    loc: Localization,
    elapsedSeconds: Int,
    moves: Int,
    hintsRemaining: Int,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Theme.text)
        }
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatGroup(loc.t("time"), formatTime(elapsedSeconds))
            StatGroup(loc.t("moves"), moves.toString())
            StatGroup(loc.t("hintsLeft"), hintsRemaining.toString())
        }
        IconButton(onClick = onRestart) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Theme.text)
        }
    }
}

@Composable
private fun StatGroup(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Theme.text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Theme.textFaint, fontSize = 10.sp)
    }
}

@Composable
private fun ActionRow(
    loc: Localization,
    notesMode: Boolean,
    hintsRemaining: Int,
    canUndo: Boolean,
    onToggleNotes: () -> Unit,
    onUndo: () -> Unit,
    onHint: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        ActionButton(
            icon = Icons.Default.EditNote,
            label = loc.t("notes"),
            active = notesMode,
            onClick = onToggleNotes
        )
        ActionButton(
            icon = Icons.Default.Undo,
            label = loc.t("undo"),
            active = false,
            disabled = !canUndo,
            onClick = onUndo
        )
        ActionButton(
            icon = Icons.Default.Lightbulb,
            label = "${loc.t("hint")} ($hintsRemaining)",
            active = false,
            disabled = hintsRemaining <= 0,
            onClick = onHint
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    disabled: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !disabled,
                onClick = onClick
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(44.dp)
                .background(
                    if (active) Theme.accent else Theme.bgPanel2,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = when {
                    disabled -> Theme.textFaint
                    active -> Theme.bg
                    else -> Theme.text
                }
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = if (disabled) Theme.textFaint else Theme.textDim,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ToastView(message: String) {
    Box(Modifier.fillMaxSize().padding(bottom = 32.dp), contentAlignment = Alignment.BottomCenter) {
        Box(
            Modifier
                .background(Theme.bgPanel2, RoundedCornerShape(50))
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text(message, color = Theme.text, fontSize = 13.sp)
        }
    }
}
