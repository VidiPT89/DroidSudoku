package com.vidi.droidsudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidi.droidsudoku.engine.ChallengeOutcome
import com.vidi.droidsudoku.engine.RecordOutcome
import com.vidi.droidsudoku.i18n.Localization
import com.vidi.droidsudoku.ui.theme.Theme

@Composable
fun ModalScaffold(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Theme.bg.copy(alpha = 0.78f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .widthIn(max = 360.dp)
                .padding(24.dp)
                .background(Theme.bgPanel, RoundedCornerShape(16.dp))
                .border(1.dp, Theme.borderStrong, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            content()
        }
    }
}

@Composable
fun WinModal(
    loc: Localization,
    elapsedSeconds: Int,
    hintsUsed: Int,
    bestTimeSeconds: Int?,
    bestHints: Int?,
    recordOutcome: RecordOutcome,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    ModalScaffold {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎉", fontSize = 40.sp)
            Spacer(Modifier.height(10.dp))
            Text(loc.t("winTitle"), color = Theme.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(loc.t("winSubtitle"), color = Theme.textDim, fontSize = 14.sp)

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBlock(loc.t("time"), formatTime(elapsedSeconds))
                StatBlock(loc.t("hintsLeft"), hintsUsed.toString())
            }

            Spacer(Modifier.height(16.dp))
            LeaderboardBlock(loc, bestTimeSeconds, bestHints, recordOutcome)

            Spacer(Modifier.height(20.dp))
            PrimaryButton(loc.t("playAgain"), onPlayAgain)
            Spacer(Modifier.height(10.dp))
            GhostButton(loc.t("backToMenu"), onBackToMenu)
        }
    }
}

@Composable
fun LeaderboardBlock(
    loc: Localization,
    bestTimeSeconds: Int?,
    bestHints: Int?,
    recordOutcome: RecordOutcome
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Theme.bgPanel2, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(loc.t("bestTime"), color = Theme.textDim, fontSize = 13.sp)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    bestTimeSeconds?.let { formatTime(it) } ?: "–",
                    color = Theme.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (recordOutcome.isNewBestTime) {
                    Text(loc.t("newRecordTime"), color = Theme.ok, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(loc.t("bestHints"), color = Theme.textDim, fontSize = 13.sp)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    bestHints?.toString() ?: "–",
                    color = Theme.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (recordOutcome.isNewBestHints) {
                    Text(loc.t("newRecordHints"), color = Theme.ok, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Challenge-mode win modal: level number in the title, star rating, best-time
 * delta, and Next Level / Back to Levels buttons instead of the classic
 * Leaderboard block.
 */
@Composable
fun ChallengeWinModal(
    loc: Localization,
    level: Int,
    stars: Int,
    elapsedSeconds: Int,
    outcome: ChallengeOutcome,
    onNextLevel: () -> Unit,
    onBackToLevels: () -> Unit
) {
    ModalScaffold {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎉", fontSize = 40.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                "${loc.t("level")} $level ${loc.t("levelCompleteSuffix")}",
                color = Theme.text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 1..3) {
                    Text(
                        "★",
                        color = if (i <= stars) Theme.accent else Theme.textFaint,
                        fontSize = 30.sp
                    )
                }
            }

            if (outcome.unlockedNext) {
                Spacer(Modifier.height(6.dp))
                Text(
                    loc.t("levelUnlocked"),
                    color = Theme.ok,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Theme.bgPanel2, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(loc.t("time"), color = Theme.textDim, fontSize = 13.sp)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            formatTime(elapsedSeconds),
                            color = Theme.text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (outcome.isNewBestTime) {
                            Text(loc.t("newRecordTime"), color = Theme.ok, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(loc.t("bestTime"), color = Theme.textDim, fontSize = 13.sp)
                    Text(
                        formatTime(outcome.bestTimeSeconds),
                        color = Theme.text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(loc.t("starsLabel"), color = Theme.textDim, fontSize = 13.sp)
                    Text(
                        "${outcome.bestStars} / 3",
                        color = Theme.text,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(loc.t("nextLevel"), onNextLevel)
            Spacer(Modifier.height(10.dp))
            GhostButton(loc.t("backToLevels"), onBackToLevels)
        }
    }
}

@Composable
fun ConfirmModal(
    title: String,
    body: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ModalScaffold {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Theme.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(body, color = Theme.textDim, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            PrimaryButton(confirmLabel, onConfirm)
            Spacer(Modifier.height(10.dp))
            GhostButton(cancelLabel, onCancel)
        }
    }
}

@Composable
fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Theme.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Theme.textFaint, fontSize = 11.sp)
    }
}

fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
