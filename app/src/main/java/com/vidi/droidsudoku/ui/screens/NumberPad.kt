package com.vidi.droidsudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidi.droidsudoku.ui.theme.Theme

@Composable
fun NumberPad(
    remainingCounts: IntArray,
    notesMode: Boolean,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (digit in 1..9) {
            NumberPadKey(
                label = digit.toString(),
                disabled = !notesMode && remainingCounts[digit - 1] <= 0,
                onClick = { onDigit(digit) }
            )
        }
        Box(
            Modifier
                .weight(1f)
                .aspectRatio(0.75f)
                .background(Theme.bgPanel2, RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onErase
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Backspace, contentDescription = null, tint = Theme.textDim)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NumberPadKey(label: String, disabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .weight(1f)
            .aspectRatio(0.75f)
            .background(Theme.bgPanel2, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !disabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (disabled) Theme.textFaint else Theme.text,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
