package com.knight.chesstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.knight.chesstrainer.mvi.*

private val TrainerLime = Color(0xFFD9FF72)
private val TrainerMuted = Color(0xFF8A928D)
private val TrainerPanel = Color(0xFF111512)

@Composable
fun OpeningTrainerScreen(
    state: TrainerUiState,
    onSquareTap: (String) -> Unit,
    onMove: (String, String) -> Unit,
    onSelectLine: (String) -> Unit,
    onSync: () -> Unit,
    onRestart: () -> Unit,
    onFeedbackReset: () -> Unit,
    onStockfishToggle: (Boolean) -> Unit,
    onSkillChange: (Float) -> Unit,
) {
    LaunchedEffect(state.feedback) {
        if (state.feedback != MoveFeedback.Idle) { delay(1800); onFeedbackReset() }
    }
    val line = state.openingLines.firstOrNull { it.id == state.selectedOpeningId }
    if (line == null) {
        Header("Opening library", "Train")
        Text("Your offline repertoire is empty. Sync the opening library to get started.", color = Color.White)
        OutlinedAction("Sync opening library", onSync)
        return
    }
    val expected = line.plies.getOrNull(state.openingPlyIndex).takeIf { state.onRepertoireLine }
    val totalWhiteMoves = (line.plies.size + 1) / 2
    val completedWhiteMoves = ((state.openingPlyIndex + 1) / 2).coerceAtMost(totalWhiteMoves)
    val displayedMoveNumber = when {
        state.openingComplete -> totalWhiteMoves
        state.opponentThinking -> completedWhiteMoves.coerceAtLeast(1)
        else -> (completedWhiteMoves + 1).coerceAtMost(totalWhiteMoves.coerceAtLeast(1))
    }
    val progress = if (totalWhiteMoves == 0) 1f else completedWhiteMoves.toFloat() / totalWhiteMoves
    val lineListState = rememberLazyListState()
    val movesListState = rememberLazyListState()
    val displayLines = listOf(line) + state.openingLines.filterNot { it.id == line.id }
    LaunchedEffect(state.selectedOpeningId) { lineListState.scrollToItem(0) }
    LaunchedEffect(state.openingPlyIndex, line.id) {
        val index = state.openingPlyIndex.coerceAtMost(line.plies.lastIndex.coerceAtLeast(0))
        movesListState.animateScrollToItem(index)
    }

    Header("${line.opening} Trainer", "${line.variation}  ·  ${state.openingLines.size} lines", trailing = {
        Text(if (state.repertoireSyncBusy) "SYNCING" else "SYNC ↻", Modifier.clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1B211C)).clickableNoRipple(onSync).padding(horizontal = 10.dp, vertical = 7.dp),
            color = TrainerLime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    })

    Row(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color(0xFF242925))) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(.02f, 1f)).fillMaxHeight().background(TrainerLime))
    }
    Text("LINE $displayedMoveNumber OF ${totalWhiteMoves.coerceAtLeast(1)}  ·  ${if (state.opponentThinking) "BLACK REPLIES" else if (state.openingComplete) "LINE COMPLETE" else "WHITE TO MOVE"}",
        Modifier.padding(top = 7.dp), color = TrainerMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

    LazyRow(Modifier.fillMaxWidth().padding(top = 12.dp), state = lineListState, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        items(displayLines, key = { it.id }) { choice ->
            val active = choice.id == state.selectedOpeningId
            Text(choice.variation, Modifier.clip(RoundedCornerShape(14.dp))
                .background(if (active) TrainerLime else Color(0xFF1B211C))
                .border(1.dp, if (active) TrainerLime else Color(0xFF303832), RoundedCornerShape(14.dp))
                .clickableNoRipple { onSelectLine(choice.id) }
                .padding(horizontal = 11.dp, vertical = 8.dp),
                color = if (active) Color(0xFF070908) else Color(0xFFD5DBD6), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    Text(line.summary, Modifier.fillMaxWidth().padding(top = 9.dp, bottom = 7.dp), color = Color(0xFFA5ADA7), fontSize = 10.sp, lineHeight = 15.sp)

    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
        .background(if (state.stockfishOpponent) Color(0xFF1B211C) else TrainerPanel)
        .border(1.dp, if (state.stockfishOpponent) TrainerLime.copy(alpha = .55f) else Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
        .padding(horizontal = 12.dp, vertical = 9.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (state.stockfishOpponent) "STOCKFISH MATCH" else "PRACTICE WITH STOCKFISH", color = TrainerLime, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
                Text(if (!state.stockfishAvailable) "Engine unavailable on this device" else state.stockfishMessage, color = TrainerMuted, fontSize = 8.sp)
            }
            Text(if (state.stockfishOpponent) "END MATCH" else "PLAY", Modifier.clip(RoundedCornerShape(9.dp))
                .background(if (state.stockfishAvailable) TrainerLime else Color(0xFF303832))
                .clickableNoRipple { onStockfishToggle(!state.stockfishOpponent) }
                .padding(horizontal = 11.dp, vertical = 8.dp),
                color = if (state.stockfishAvailable) Color(0xFF070908) else TrainerMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        if (state.stockfishOpponent && state.stockfishAvailable) {
            Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("STRENGTH ${state.engineSkill.toInt()}/20", Modifier.width(96.dp), color = TrainerMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Slider(value = state.engineSkill, onValueChange = onSkillChange, valueRange = 0f..20f, modifier = Modifier.weight(1f).height(36.dp),
                    colors = SliderDefaults.colors(thumbColor = TrainerLime, activeTrackColor = TrainerLime, inactiveTrackColor = Color(0xFF3A403B)))
            }
            Text("Your selected variation guides the opening. After a different move, keep playing freely against the engine.", color = TrainerMuted, fontSize = 8.sp, lineHeight = 12.sp)
        }
    }

    ChessBoard(
        pieces = state.chessPosition.pieces,
        ghost = expected?.uci?.substring(2, 4)?.takeIf { !state.openingComplete && !state.opponentThinking },
        selected = state.selectedSquare,
        feedback = state.feedback.name.lowercase(),
        onSquare = onSquareTap,
        boardTheme = state.boardTheme,
        motionEnabled = state.motionEnabled,
        legalDestinations = state.legalDestinations,
        lastMove = state.lastBoardMove,
        onMove = onMove,
        ghostPiece = expected?.uci?.take(2)?.let { state.chessPosition.pieces[it] },
    )

    FeedbackCard(
        feedback = when { state.openingComplete -> "correct"; state.opponentThinking -> "correct"; else -> state.feedback.name.lowercase() },
        guide = when {
            state.openingComplete -> "Line complete. Play it again or choose another variation above."
            state.opponentThinking -> "${state.coachMessage}  Opponent replies…"
            else -> state.coachMessage
        },
    )

    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(TrainerPanel)
        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(14.dp)).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("MOVE GUIDE", color = TrainerMuted, fontSize = 8.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                Text(if (state.openingComplete) "Variation complete" else if (state.opponentThinking) "Black is replying" else "Your move: ${expected?.san ?: "—"}",
                    Modifier.padding(top = 3.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("$displayedMoveNumber…",
                color = TrainerLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(expected?.guide ?: if (state.openingComplete) "You have reached the end of this prepared line." else line.summary,
            Modifier.padding(top = 5.dp), color = Color(0xFFA5ADA7), fontSize = 9.sp, lineHeight = 14.sp)

        LazyRow(Modifier.fillMaxWidth().padding(top = 10.dp), state = movesListState, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            itemsIndexed(line.plies) { index, ply ->
                val active = index == state.openingPlyIndex
                val played = index < state.openingPlyIndex
                Text(ply.san, Modifier.clip(RoundedCornerShape(7.dp))
                    .background(if (active) TrainerLime else if (played) Color(0xFF273128) else Color(0xFF1A1F1B))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                    color = if (active) Color(0xFF070908) else if (played) Color(0xFFC9E88F) else Color(0xFF778079), fontSize = 9.sp)
            }
        }
    }
    Text(state.repertoireMessage, Modifier.fillMaxWidth().padding(top = 7.dp), color = TrainerMuted, fontSize = 8.sp, lineHeight = 12.sp)
    Row(Modifier.fillMaxWidth().padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedAction("↻  Restart line", onRestart, Modifier.weight(1f))
        OutlinedAction(if (state.repertoireSyncBusy) "Syncing…" else "Download updates", onSync, Modifier.weight(1f))
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = clickable(onClick = onClick)
