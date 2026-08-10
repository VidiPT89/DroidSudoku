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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidi.droidsudoku.data.Difficulty
import com.vidi.droidsudoku.i18n.Localization
import com.vidi.droidsudoku.ui.theme.Theme

@Composable
fun MainMenuScreen(
    loc: Localization,
    hasSave: Boolean,
    selectedDifficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    onPlay: () -> Unit,
    onContinue: () -> Unit,
    onHowToPlay: () -> Unit,
    onToggleLang: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        BackgroundGlow()

        Box(Modifier.fillMaxSize().padding(20.dp)) {
            Box(Modifier.align(Alignment.TopEnd)) {
                LangToggle(loc, onToggleLang)
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                loc.t("menuTag"),
                color = Theme.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(10.dp))
            BrandLogo(sizeSp = 40)
            Spacer(Modifier.height(14.dp))
            Text(
                loc.t("menuSubtitle"),
                color = Theme.textDim,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))
            DifficultyPicker(loc, selectedDifficulty, onDifficultyChange)

            Spacer(Modifier.height(36.dp))
            if (hasSave) {
                SecondaryButton(loc.t("continueGame"), onContinue)
                Spacer(Modifier.height(12.dp))
            }
            PrimaryButton(loc.t("play"), onPlay)
            Spacer(Modifier.height(12.dp))
            GhostButton(loc.t("howToPlay"), onHowToPlay)

            Spacer(Modifier.height(48.dp))
            FooterCredits(loc)
            Spacer(Modifier.height(2.dp))
            Text("ividi.dev · github.com/VidiPT89", color = Theme.accent, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DifficultyPicker(
    loc: Localization,
    selected: Difficulty,
    onChange: (Difficulty) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            loc.t("difficultyLabel").uppercase(),
            color = Theme.textFaint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier
                .background(Theme.bgPanel2, RoundedCornerShape(50))
                .padding(4.dp)
        ) {
            listOf(
                Difficulty.EASY to loc.t("easy"),
                Difficulty.MEDIUM to loc.t("medium"),
                Difficulty.HARD to loc.t("hard")
            ).forEach { (difficulty, label) ->
                val isSelected = difficulty == selected
                Box(
                    Modifier
                        .background(if (isSelected) Theme.accent else Color.Transparent, RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onChange(difficulty) }
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text(
                        label,
                        color = if (isSelected) Theme.bg else Theme.textDim,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
