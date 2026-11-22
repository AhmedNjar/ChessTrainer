package com.knight.chesstrainer.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knight.chesstrainer.mvi.AnimatedBoardMove

private val ColorNight = Color(0xFF070908)
private val ColorPanel = Color(0xFF131715)
private val ColorLime = Color(0xFFD9FF72)
private val ColorTeal = Color(0xFF63F5CE)
private val ColorCoral = Color(0xFFFF796F)
private val ColorMuted = Color(0xFF8A928D)
private val BorderLine = Color(0x1AFFFFFF)

@Composable
fun Header(
    title: String,
    eyebrow: String,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                color = ColorMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp
            )
            Text(
                text = title,
                color = Color(0xFFF7F8F4),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun SectionTitle(
    eyebrow: String,
    title: String,
    trailing: String? = null,
    onTrailing: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 11.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                color = ColorMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        trailing?.let { text ->
            val clickModifier = if (onTrailing != null) Modifier.clickable(onClick = onTrailing) else Modifier
            Text(
                text = text,
                modifier = Modifier
                    .then(clickModifier)
                    .padding(6.dp),
                color = ColorLime,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FeedbackCard(feedback: String, guide: String? = null) {
    val title = when (feedback) {
        "correct" -> "Excellent move"
        "wrong" -> "Not quite"
        else -> "Your move"
    }
    val fallbackBody = when (feedback) {
        "correct" -> "Nf3 controls the center and prepares castling."
        "wrong" -> "The target square is glowing. Try again."
        else -> "Move the knight to the highlighted position."
    }

    val isWrong = feedback == "wrong"
    val isCorrect = feedback == "correct"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isWrong) ColorCoral.copy(alpha = 0.08f) else ColorPanel)
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isCorrect) "✓" else if (isWrong) "×" else "♘",
            modifier = Modifier.padding(end = 11.dp),
            color = if (isWrong) ColorCoral else ColorLime,
            fontSize = 21.sp
        )
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = guide?.takeIf { it.isNotBlank() } ?: fallbackBody,
                color = ColorMuted,
                fontSize = 9.sp
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
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
                        if (source != null && destination != null && source != destination) {
                            onMove?.invoke(source, destination)
                        }
                        draggedSquare = null
                    },
                    onDragCancel = { draggedSquare = null },
                )
            }
    ) {
        val squareSize = maxWidth / 8
        val squarePx = with(LocalDensity.current) { squareSize.toPx() }
        val movingPiece = lastMove
        val moveProgress = movingPiece?.let { move ->
            remember(move.id) { Animatable(if (motionEnabled) 0f else 1f) }
        }

        LaunchedEffect(movingPiece?.id, motionEnabled) {
            if (movingPiece != null && moveProgress != null) {
                if (motionEnabled) {
                    moveProgress.snapTo(0f)
                    moveProgress.animateTo(1f, spring(dampingRatio = 0.78f, stiffness = 420f))
                } else {
                    moveProgress.snapTo(1f)
                }
            }
        }

        val movingDestination = movingPiece?.to?.takeIf {
            moveProgress?.value?.let { progress -> progress < 1f } == true
        }

        val squareColors = when (boardTheme) {
            "Wood" -> Color(0xFFE8C99B) to Color(0xFF9B6848)
            "Modern" -> Color(0xFFE1E4D9) to Color(0xFF7E9182)
            else -> Color(0xFFD6D7CA) to Color(0xFF4D5851)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(3.dp))
        ) {
            repeat(8) { row ->
                Row(modifier = Modifier.weight(1f)) {
                    repeat(8) { col ->
                        val square = "${'a' + col}${8 - row}"
                        val isSelected = selected == square
                        val scale by animateFloatAsState(
                            targetValue = if (motionEnabled && isSelected) 1.12f else 1f,
                            label = "pieceScale"
                        )
                        val isLightSquare = (row + col) % 2 == 0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (isLightSquare) squareColors.first else squareColors.second)
                                .then(if (isSelected) Modifier.border(2.dp, ColorLime.copy(alpha = 0.9f)) else Modifier)
                                .clickable { onSquare(square) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (ghost == square) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(0.74f)
                                        .border(2.dp, ColorLime.copy(alpha = 0.72f), CircleShape)
                                )
                            }
                            if (square in legalDestinations) {
                                if (pieces[square] == null) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x99080A08))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.94f)
                                            .border(3.dp, Color(0xAA080A08), CircleShape)
                                    )
                                }
                            }
                            pieces[square]?.takeUnless { square == movingDestination || square == draggedSquare }?.let { piece ->
                                Text(
                                    text = pieceGlyph(piece).toString(),
                                    modifier = Modifier
                                        .scale(scale)
                                        .graphicsLayer {
                                            if (isSelected) {
                                                scaleX = 1.08f
                                                scaleY = 1.08f
                                            }
                                        },
                                    color = if (piece.isUpperCase()) Color(0xFFFAFBF4) else Color(0xFF1A1E1B),
                                    fontSize = 31.sp,
                                    lineHeight = 32.sp
                                )
                            }
                            if (ghost == square) {
                                Text(
                                    text = pieceGlyph(ghostPiece ?: 'N').toString(),
                                    color = Color.White.copy(alpha = 0.38f),
                                    fontSize = 29.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (movingPiece != null && moveProgress != null && moveProgress.value < 1f) {
            val fromX = movingPiece.from[0] - 'a'
            val fromY = 8 - (movingPiece.from[1] - '0')
            val toX = movingPiece.to[0] - 'a'
            val toY = 8 - (movingPiece.to[1] - '0')
            val x = (fromX + (toX - fromX) * moveProgress.value) * squareSize.value
            val y = (fromY + (toY - fromY) * moveProgress.value) * squareSize.value

            Text(
                text = pieceGlyph(movingPiece.piece).toString(),
                modifier = Modifier
                    .offset { IntOffset((x * density).toInt(), (y * density).toInt()) }
                    .size(squareSize),
                color = if (movingPiece.piece.isUpperCase()) Color(0xFFFAFBF4) else Color(0xFF1A1E1B),
                fontSize = 31.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Center
            )
        }

        draggedSquare?.let { from ->
            pieces[from]?.let { piece ->
                Text(
                    text = pieceGlyph(piece).toString(),
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (dragPosition.x - squarePx / 2).toInt(),
                                (dragPosition.y - squarePx / 2).toInt()
                            )
                        }
                        .size(squareSize)
                        .graphicsLayer {
                            scaleX = 1.12f
                            scaleY = 1.12f
                        },
                    color = if (piece.isUpperCase()) Color(0xFFFAFBF4) else Color(0xFF1A1E1B),
                    fontSize = 34.sp,
                    lineHeight = 35.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PrimaryButton(
    icon: String,
    eyebrow: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFFBDE959), Color(0xFFDCFF7F))))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon.isNotBlank()) {
            Text(
                text = icon,
                modifier = Modifier.padding(end = 11.dp),
                color = ColorNight,
                fontSize = 22.sp
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow.isNotBlank()) {
                Text(
                    text = eyebrow,
                    color = ColorNight.copy(alpha = 0.65f),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = label,
                color = ColorNight,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(text = "›", color = ColorNight, fontSize = 24.sp)
    }
}

@Composable
fun OutlinedAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color(0xFF161A17))
            .border(1.dp, BorderLine, RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFFB9C0BB),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
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
