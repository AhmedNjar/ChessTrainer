package com.knight.chesstrainer.ui
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch
import com.knight.chesstrainer.mvi.AnimatedBoardMove
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
private val Night=Color(0xFF070908); private val Panel=Color(0xFF131715); private val Lime=Color(0xFFD9FF72); private val Teal=Color(0xFF63F5CE); private val Coral=Color(0xFFFF796F); private val Muted=Color(0xFF8A928D); private val Line=Color(0x1AFFFFFF)
@Composable
fun Header(title: String, eyebrow: String, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(eyebrow.uppercase(), color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
            Text(title, color = Color(0xFFF7F8F4), fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
        }
        trailing?.invoke()
    }
}

@Composable
fun SectionTitle(eyebrow: String, title: String, trailing: String? = null, onTrailing: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 11.dp), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(eyebrow.uppercase(), color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        trailing?.let { Text(it, Modifier.then(if (onTrailing != null) Modifier.clickable(onClick = onTrailing) else Modifier).padding(6.dp), color = Lime, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
fun FeedbackCard(feedback: String, guide: String? = null) {
    val title = when (feedback) { "correct" -> "Excellent move"; "wrong" -> "Not quite"; else -> "Your move" }
    val body = when (feedback) { "correct" -> "Nf3 controls the center and prepares castling."; "wrong" -> "The target square is glowing. Try again."; else -> "Move the knight to the ghost position." }
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp).clip(RoundedCornerShape(14.dp)).background(if (feedback == "wrong") Coral.copy(alpha = .08f) else Panel).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(if (feedback == "correct") "✓" else if (feedback == "wrong") "×" else "♘", Modifier.padding(end = 11.dp), color = if (feedback == "wrong") Coral else Lime, fontSize = 21.sp)
        Column {
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(guide?.takeIf { it.isNotBlank() } ?: body, color = Muted, fontSize = 9.sp)
        }
    }
}

@Composable
fun ChessBoard(
    pieces: Map<String, Char>,
    ghost: String?,
    selected: String?,
    feedback: String,
    onSquare: (String) -> Unit,
    boardTheme: String = "Neo",
    motionEnabled: Boolean = true,
    legalDestinations: Set<String> = emptySet(),
    lastMove: AnimatedBoardMove? = null,
    onMove: ((String, String) -> Unit)? = null,
    ghostPiece: Char? = null,
) {
    var draggedSquare by remember { mutableStateOf<String?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    BoxWithConstraints(
        Modifier.fillMaxWidth().aspectRatio(1f)
            .pointerInput(Unit) {
                var from: String? = null
                var to: String? = null
                fun at(offset: Offset): String {
                    val col = (offset.x / (size.width / 8f)).toInt().coerceIn(0, 7)
                    val row = (offset.y / (size.height / 8f)).toInt().coerceIn(0, 7)
                    return "${'a' + col}${8 - row}"
                }
                detectDragGestures(
                    onDragStart = { offset ->
                        from = at(offset)
                        to = from
                        if (pieces[from] != null) {
                            draggedSquare = from
                            dragPosition = offset
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragPosition = change.position
                        to = at(change.position)
                    },
                    onDragEnd = {
                        val source = from
                        val destination = to
                        if (source != null && destination != null && source != destination) onMove?.invoke(source, destination)
                        draggedSquare = null
                    },
                    onDragCancel = { draggedSquare = null },
                )
            },
    ) {
        val squareSize = maxWidth / 8
        val squarePx = with(LocalDensity.current) { squareSize.toPx() }
        val movingPiece = lastMove
        val moveProgress = movingPiece?.let { move -> remember(move.id) { Animatable(if (motionEnabled) 0f else 1f) } }
        LaunchedEffect(movingPiece?.id, motionEnabled) {
            if (movingPiece != null && moveProgress != null) {
                if (motionEnabled) {
                    moveProgress.snapTo(0f)
                    moveProgress.animateTo(1f, spring(dampingRatio = .78f, stiffness = 420f))
                } else moveProgress.snapTo(1f)
            }
        }
        val movingDestination = movingPiece?.to?.takeIf { moveProgress?.value?.let { progress -> progress < 1f } == true }
        val squareColors = when (boardTheme) {
            "Wood" -> Color(0xFFE8C99B) to Color(0xFF9B6848)
            "Modern" -> Color(0xFFE1E4D9) to Color(0xFF7E9182)
            else -> Color(0xFFD6D7CA) to Color(0xFF4D5851)
        }
        Column(Modifier.fillMaxSize().clip(RoundedCornerShape(3.dp))) {
            repeat(8) { row ->
                Row(Modifier.weight(1f)) {
                    repeat(8) { col ->
                        val square = "${'a' + col}${8 - row}"
                        val isSelected = selected == square
                        val scale by animateFloatAsState(if (motionEnabled && isSelected) 1.12f else 1f, label = "pieceScale")
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .background(if ((row + col) % 2 == 0) squareColors.first else squareColors.second)
                                .then(if (isSelected) Modifier.border(2.dp, Lime.copy(alpha = .9f)) else Modifier)
                                .clickable { onSquare(square) },
                            Alignment.Center,
                        ) {
                            if (ghost == square) Box(Modifier.fillMaxSize(.74f).border(2.dp, Lime.copy(alpha = .72f), CircleShape))
                            if (square in legalDestinations) {
                                if (pieces[square] == null) Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0x99080A08)))
                                else Box(Modifier.fillMaxSize(.94f).border(3.dp, Color(0xAA080A08), CircleShape))
                            }
                            pieces[square]?.takeUnless { square == movingDestination || square == draggedSquare }?.let { piece ->
                                Text(pieceGlyph(piece).toString(), Modifier.scale(scale).graphicsLayer {
                                    if (isSelected) { scaleX = 1.08f; scaleY = 1.08f }
                                }, color = if (piece.isUpperCase()) Color(0xFFFAFBF4) else Color(0xFF1A1E1B), fontSize = 31.sp, lineHeight = 32.sp)
                            }
                            if (ghost == square) Text(pieceGlyph(ghostPiece ?: 'N').toString(), color = Color.White.copy(alpha = .38f), fontSize = 29.sp)
                        }
                    }
                }
            }
        }
        if (movingPiece != null && moveProgress != null && moveProgress.value < 1f) {
            val fromX = (movingPiece.from[0] - 'a')
            val fromY = 8 - (movingPiece.from[1] - '0')
            val toX = (movingPiece.to[0] - 'a')
            val toY = 8 - (movingPiece.to[1] - '0')
            val x = (fromX + (toX - fromX) * moveProgress.value) * squareSize.value
            val y = (fromY + (toY - fromY) * moveProgress.value) * squareSize.value
            Text(pieceGlyph(movingPiece.piece).toString(), Modifier.offset { IntOffset((x * density).toInt(), (y * density).toInt()) }
                .size(squareSize),
                color = if (movingPiece.piece.isUpperCase()) Color(0xFFFAFBF4) else Color(0xFF1A1E1B), fontSize = 31.sp, lineHeight = 32.sp,
                textAlign = TextAlign.Center)
        }
        draggedSquare?.let { from ->
            pieces[from]?.let { piece ->
                Text(pieceGlyph(piece).toString(), Modifier.offset {
                    IntOffset((dragPosition.x - squarePx / 2).toInt(), (dragPosition.y - squarePx / 2).toInt())
                }.size(squareSize).graphicsLayer { scaleX = 1.12f; scaleY = 1.12f },
                    color = if (piece.isUpperCase()) Color(0xFFFAFBF4) else Color(0xFF1A1E1B), fontSize = 34.sp, lineHeight = 35.sp,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun PrimaryButton(icon: String, eyebrow: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().heightIn(min = 54.dp).clip(RoundedCornerShape(15.dp)).background(Brush.horizontalGradient(listOf(Color(0xFFBDE959), Color(0xFFDCFF7F))))
        .clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (icon.isNotBlank()) Text(icon, Modifier.padding(end = 11.dp), color = Night, fontSize = 22.sp)
        Column(Modifier.weight(1f)) {
            if (eyebrow.isNotBlank()) Text(eyebrow, color = Night.copy(alpha = .65f), fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(label, color = Night, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
        Text("›", color = Night, fontSize = 24.sp)
    }
}

@Composable
fun OutlinedAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(45.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF161A17))
        .border(1.dp, Line, RoundedCornerShape(13.dp)).clickable(onClick = onClick), Alignment.Center) {
        Text(label, color = Color(0xFFB9C0BB), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private fun pieceGlyph(piece: Char): Char = when (piece.lowercaseChar()) {
    'k' -> if (piece.isUpperCase()) '♔' else '♚'
    'q' -> if (piece.isUpperCase()) '♕' else '♛'
    'r' -> if (piece.isUpperCase()) '♖' else '♜'
    'b' -> if (piece.isUpperCase()) '♗' else '♝'
    'n' -> if (piece.isUpperCase()) '♘' else '♞'
    else -> if (piece.isUpperCase()) '♙' else '♟'
}

private fun trainingPosition() = mapOf(
    "a8" to 'r', "b8" to 'n', "c8" to 'b', "d8" to 'q', "e8" to 'k', "f8" to 'b', "h8" to 'r',
    "a7" to 'p', "b7" to 'p', "c7" to 'p', "d7" to 'p', "e7" to 'p', "f7" to 'p', "g7" to 'p', "h7" to 'p',
    "e5" to 'p', "c6" to 'n', "b5" to 'B', "e4" to 'P', "a2" to 'P', "b2" to 'P', "c2" to 'P', "d2" to 'P', "e2" to 'P', "f2" to 'P', "g2" to 'P', "h2" to 'P',
    "a1" to 'R', "b1" to 'N', "c1" to 'B', "d1" to 'Q', "e1" to 'K', "f1" to 'B', "g1" to 'N', "h1" to 'R',
)

@Composable
fun PuzzleScreen(onNext: () -> Unit) {
    var solved by remember { mutableStateOf(false) }
    Header("Daily Tactics", "Puzzle 5 of 5")
    Box(Modifier.fillMaxWidth().padding(top = 35.dp), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (solved) "BRILLIANT!" else "TACTICAL FINISH", color = Lime, fontSize = if (solved) 29.sp else 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            Text("+25 XP  ·  +5 ELO", color = Teal, fontSize = 10.sp)
        }
    }
    val mate = mapOf("g8" to 'k', "f7" to 'p', "g7" to 'p', "h7" to 'p', "e6" to 'p', "g6" to 'Q', "a2" to 'P', "b2" to 'P', "f2" to 'P', "g2" to 'P', "h2" to 'P', "g1" to 'K', "a8" to 'r')
    ChessBoard(mate, null, null, if (solved) "correct" else "idle", onSquare = { solved = true })
    Row(Modifier.fillMaxWidth().padding(top = 14.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF10201B)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("♛", color = Teal, fontSize = 29.sp)
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text("RANK PROGRESS", color = Muted, fontSize = 8.sp, letterSpacing = 1.sp)
            Text("Tactic Trainee · Level 6", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("+25 XP\n+5 ELO", color = Teal, fontSize = 11.sp, textAlign = TextAlign.End, lineHeight = 18.sp)
    }
    PrimaryButton("✦", "", "Next Puzzle", onNext, Modifier.padding(top = 13.dp))
    OutlinedAction("Analyze position", { solved = true }, Modifier.padding(top = 8.dp))
}
