package com.vidi.droidsudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidi.droidsudoku.data.Difficulty
import com.vidi.droidsudoku.engine.Challenge
import com.vidi.droidsudoku.engine.ChallengeProgress
import com.vidi.droidsudoku.i18n.Localization
import com.vidi.droidsudoku.ui.theme.Theme
import kotlin.math.max

private fun bandColor(level: Int): Color = when (Challenge.bandForLevel(level)) {
    Difficulty.EASY -> Theme.accentLight
    Difficulty.MEDIUM -> Theme.accent
    Difficulty.HARD -> Theme.accentDark
    Difficulty.EXPERT -> Theme.danger
}

@Composable
fun ChallengesScreen(
    loc: Localization,
    progress: ChallengeProgress,
    onBack: () -> Unit,
    onPickLevel: (Int) -> Unit
) {
    // Oldest level kept on screen; only ever walked backwards by "show earlier".
    var windowStart by remember {
        mutableIntStateOf(max(1, progress.highestUnlocked - Challenge.LEVELS_BEHIND + 1))
    }
    val end = progress.highestUnlocked + Challenge.LEVELS_AHEAD
    val levels = (windowStart..end).toList()

    Box(Modifier.fillMaxSize()) {
        BackgroundGlow()

        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Theme.text)
                }
                Text(
                    loc.t("challengesTitle"),
                    color = Theme.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SummaryStat(loc.t("levelReached"), progress.highestUnlocked.toString())
                Spacer(Modifier.width(40.dp))
                SummaryStat(loc.t("starsLabel"), progress.totalStars.toString())
            }
            Text(
                loc.t("challengesSubtitle"),
                color = Theme.textDim,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
            )

            if (windowStart > 1) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)) {
                    GhostButton(loc.t("showEarlier")) {
                        windowStart = max(1, windowStart - Challenge.LEVELS_BEHIND)
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 72.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                items(levels, key = { it }) { level ->
                    LevelTile(
                        level = level,
                        unlocked = progress.isUnlocked(level),
                        isCurrent = level == progress.highestUnlocked,
                        stars = progress.starsAt(level),
                        onClick = { onPickLevel(level) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label.uppercase(), color = Theme.textFaint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Theme.accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LevelTile(
    level: Int,
    unlocked: Boolean,
    isCurrent: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    val band = bandColor(level)
    val borderColor = when {
        isCurrent -> band
        stars > 0 -> band.copy(alpha = 0.55f)
        else -> Theme.border
    }
    Box(
        Modifier
            .aspectRatio(1f)
            .background(if (unlocked) Theme.bgPanel2 else Theme.bgPanel, RoundedCornerShape(12.dp))
            .border(if (isCurrent) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .then(
                if (unlocked) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!unlocked) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Theme.textFaint)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(level.toString(), color = Theme.text, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Row {
                    for (i in 1..3) {
                        Text(
                            "★",
                            color = if (i <= stars) band else Theme.textFaint,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
