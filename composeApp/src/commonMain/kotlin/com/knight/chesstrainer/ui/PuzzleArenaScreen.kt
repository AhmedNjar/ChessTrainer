package com.knight.chesstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knight.chesstrainer.mvi.TrainerUiState
import com.knight.chesstrainer.puzzles.StarterPuzzleSet

@Composable
fun PuzzleArenaScreen(
    state: TrainerUiState,
    onSquareTap: (String) -> Unit,
    onNext: () -> Unit,
    onAnalyze: () -> Unit
) {
    Header(
        title = "Daily Tactics",
        eyebrow = "Puzzle ${state.puzzleIndex + 1} of ${StarterPuzzleSet.records.size}"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 35.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (state.puzzleSolved) "BRILLIANT!" else "TACTICAL FINISH",
                color = Color(0xFFD9FF72),
                fontSize = if (state.puzzleSolved) 29.sp else 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                text = if (state.puzzleSolved) "+25 XP  ·  +5 ELO" else "Solve to earn +25 XP  ·  +5 ELO",
                color = Color(0xFF63F5CE),
                fontSize = 10.sp
            )
        }
    }

    val feedbackStyle = when {
        state.puzzleSolved -> "correct"
        state.puzzleFeedback.startsWith("Not") -> "wrong"
        else -> "idle"
    }

    ChessBoard(
        pieces = state.puzzlePieces,
        ghost = null,
        selected = state.selectedPuzzleSquare,
        feedback = feedbackStyle,
        onSquare = onSquareTap,
        boardTheme = state.boardTheme,
        motionEnabled = state.motionEnabled
    )

    if (state.puzzleFeedback.isNotBlank()) {
        Text(
            text = state.puzzleFeedback,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = if (state.puzzleSolved) Color(0xFF63F5CE) else Color(0xFFD9FF72),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF10201B))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "♛",
            color = Color(0xFF63F5CE),
            fontSize = 29.sp
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = "RANK PROGRESS",
                color = Color(0xFF8A928D),
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "Tactic Trainee · Level 6",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = "+25 XP\n+5 ELO",
            color = Color(0xFF63F5CE),
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            lineHeight = 18.sp
        )
    }

    PrimaryButton(
        icon = "✦",
        eyebrow = "",
        label = "Next Puzzle",
        onClick = onNext,
        modifier = Modifier.padding(top = 13.dp)
    )

    OutlinedAction(
        label = "Analyze position",
        onClick = onAnalyze,
        modifier = Modifier.padding(top = 8.dp)
    )
}
