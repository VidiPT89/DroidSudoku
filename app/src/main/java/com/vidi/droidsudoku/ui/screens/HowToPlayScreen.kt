package com.vidi.droidsudoku.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidi.droidsudoku.i18n.Localization
import com.vidi.droidsudoku.ui.theme.Theme

@Composable
fun HowToPlayScreen(loc: Localization, onClose: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        BackgroundGlow()

        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Theme.text)
                }
                Text(loc.t("htpTitle"), color = Theme.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Text(loc.t("htpIntro"), color = Theme.textDim, fontSize = 15.sp)

                Spacer(Modifier.height(28.dp))
                HtpDiagramRow()

                Spacer(Modifier.height(28.dp))
                HtpSection(loc.t("htpRuleTitle"), loc.t("htpRuleBody"))
                HtpSection(loc.t("htpGivenTitle"), loc.t("htpGivenBody"))
                HtpSection(loc.t("htpConflictTitle"), loc.t("htpConflictBody"))
                HtpSection(loc.t("htpNotesTitle"), loc.t("htpNotesBody"))

                Spacer(Modifier.height(12.dp))
                Text(
                    loc.t("htpToolsTitle"),
                    color = Theme.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                ToolRow(loc.t("htpHintTool"))
                ToolRow(loc.t("htpUndoTool"))

                Spacer(Modifier.height(28.dp))
            }

            Box(Modifier.padding(24.dp)) {
                PrimaryButton(loc.t("htpCloseButton"), onClose)
            }
        }
    }
}

@Composable
private fun HtpSection(title: String, body: String) {
    Column(Modifier.padding(bottom = 20.dp)) {
        Text(title, color = Theme.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(body, color = Theme.textDim, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun ToolRow(text: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text("•  ", color = Theme.accent, fontSize = 14.sp)
        Text(text, color = Theme.textDim, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

/** A tiny 3x3 grid diagram illustrating a conflict: two cells sharing "7" in the same row. */
@Composable
private fun HtpDiagramRow() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(Modifier.background(Theme.bgPanel2, RoundedCornerShape(10.dp)).padding(10.dp)) {
            for (r in 0 until 3) {
                Row {
                    for (c in 0 until 3) {
                        val isConflict = r == 1 && (c == 0 || c == 2)
                        val digit = when {
                            r == 1 && c == 0 -> "7"
                            r == 1 && c == 2 -> "7"
                            r == 0 && c == 1 -> "4"
                            else -> ""
                        }
                        Box(
                            Modifier
                                .size(32.dp)
                                .padding(1.dp)
                                .background(
                                    if (isConflict) Theme.conflictBg else Theme.bgPanel,
                                    RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                digit,
                                color = if (isConflict) Theme.conflictText else Theme.userDigit,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
