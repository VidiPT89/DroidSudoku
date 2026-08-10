package com.vidi.droidsudoku.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vidi.droidsudoku.data.SudokuCell
import com.vidi.droidsudoku.ui.theme.Theme
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun SudokuGridView(
    cells: List<SudokuCell>,
    selectedIndex: Int?,
    shakeTokens: Map<Int, Int>,
    onCellTap: (Int) -> Unit
) {
    val selectedValue = selectedIndex?.let { cells[it].value }
    val selectedRow = selectedIndex?.let { it / 9 }
    val selectedCol = selectedIndex?.let { it % 9 }
    val selectedBox = selectedIndex?.let { cells[it].boxIndex }

    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Theme.bgPanel, RoundedCornerShape(12.dp))
            .border(2.dp, Theme.borderStrong, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        for (row in 0 until 9) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                for (col in 0 until 9) {
                    val index = row * 9 + col
                    val cell = cells[index]
                    val isSelected = index == selectedIndex
                    val isPeer = selectedRow == row || selectedCol == col || selectedBox == cell.boxIndex
                    val isSameNumber = selectedValue != null && cell.value == selectedValue && !isSelected

                    SudokuCellView(
                        cell = cell,
                        isSelected = isSelected,
                        isPeer = isPeer && !isSelected,
                        isSameNumber = isSameNumber,
                        shakeToken = shakeTokens[index] ?: 0,
                        thickRight = col == 2 || col == 5,
                        thickBottom = row == 2 || row == 5,
                        onTap = { onCellTap(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.SudokuCellView(
    cell: SudokuCell,
    isSelected: Boolean,
    isPeer: Boolean,
    isSameNumber: Boolean,
    shakeToken: Int,
    thickRight: Boolean,
    thickBottom: Boolean,
    onTap: () -> Unit
) {
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(shakeToken) {
        if (shakeToken > 0) {
            shakeAnim.snapTo(0f)
            shakeAnim.animateTo(1f, animationSpec = tween(360, easing = LinearEasing))
        }
    }
    val shakeOffset = 4f * sin(shakeAnim.value * PI.toFloat() * 6f) * (1f - shakeAnim.value)

    val bg = when {
        cell.isConflict -> Theme.conflictBg
        isSelected -> Theme.selectedCellBg
        isSameNumber -> Theme.sameNumberHighlightBg
        isPeer -> Theme.peerHighlightBg
        else -> Theme.bgPanel2
    }

    Box(
        Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(
                end = if (thickRight) 2.dp else 0.5.dp,
                bottom = if (thickBottom) 2.dp else 0.5.dp,
                start = 0.5.dp,
                top = 0.5.dp
            )
            .graphicsLayer { translationX = shakeOffset }
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cell.value != null) {
            Text(
                cell.value.toString(),
                color = if (cell.isConflict) Theme.conflictText else if (cell.isGiven) Theme.givenDigit else Theme.userDigit,
                fontSize = 18.sp,
                fontWeight = if (cell.isGiven) FontWeight.Bold else FontWeight.SemiBold
            )
        } else if (cell.notes.isNotEmpty()) {
            NotesGrid(cell.notes)
        }
    }
}

@Composable
private fun NotesGrid(notes: Set<Int>) {
    Column(Modifier.padding(2.dp)) {
        for (r in 0 until 3) {
            Row {
                for (c in 0 until 3) {
                    val digit = r * 3 + c + 1
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            if (digit in notes) digit.toString() else "",
                            color = Theme.noteText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
