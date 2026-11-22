package com.knight.chesstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HomeNight = Color(0xFF070908)
private val HomePanel = Color(0xFF131715)
private val HomeLime = Color(0xFFD9FF72)
private val HomeTeal = Color(0xFF63F5CE)
private val HomeMuted = Color(0xFF8A928D)
private val BorderLine = Color(0x1AFFFFFF)

@Composable
fun HomeScreen(
    onTrain: () -> Unit,
    onPuzzles: () -> Unit,
    onAccount: () -> Unit
) {
    Header(
        title = "Good evening, Alex",
        eyebrow = "Tuesday · 12 day streak",
        trailing = {
            Text(
                text = "♘",
                modifier = Modifier
                    .clip(CircleShape)
                    .background(HomePanel)
                    .clickable(onClick = onAccount)
                    .padding(10.dp),
                color = HomeLime,
                fontSize = 18.sp
            )
        }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0x2ED9FF72), HomePanel, HomePanel)))
            .border(1.dp, Color(0x3BD9FF72), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(HomeLime.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "♛\nIV",
                color = HomeNight,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )
        }
        Column(modifier = Modifier.padding(start = 15.dp)) {
            Text(
                text = "OPENING RANK",
                color = HomeMuted,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "Ruy Lopez Adept",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "1,284 Puzzle ELO   +42 this week",
                color = HomeTeal,
                fontSize = 10.sp
            )
        }
    }

    SectionTitle(eyebrow = "Daily quests", title = "Keep your streak alive", trailing = "⚡ 12")

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuestCard(
            value = "60",
            title = "Solve 5 puzzles",
            caption = "3 of 5 complete",
            tint = HomeLime,
            modifier = Modifier.weight(1f),
            onClick = onPuzzles
        )
        QuestCard(
            value = "20",
            title = "Master 3 lines",
            caption = "Ruy Lopez · 1 of 3",
            tint = HomeTeal,
            modifier = Modifier.weight(1f),
            onClick = onTrain
        )
    }

    SectionTitle(
        eyebrow = "Opening map",
        title = "Your repertoire",
        trailing = "View all  ›",
        onTrailing = onTrain
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF101411))
            .border(1.dp, BorderLine, RoundedCornerShape(20.dp))
            .padding(17.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MapNode(piece = "♗", name = "Italian")
            MapNode(piece = "♘", name = "Ruy Lopez", featured = true, onClick = onTrain)
            MapNode(piece = "♙", name = "Queen's Gambit", locked = true)
        }
        Text(
            text = "68% mastered · Exchange Variation",
            color = HomeMuted,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    PrimaryButton(
        icon = "♘",
        eyebrow = "CONTINUE YOUR PATH",
        label = "Launch Training",
        onClick = onTrain,
        modifier = Modifier.padding(top = 14.dp)
    )
    OutlinedAction(
        label = "Solve a daily puzzle",
        onClick = onPuzzles,
        modifier = Modifier.padding(top = 9.dp)
    )
}

@Composable
fun QuestCard(
    value: String,
    title: String,
    caption: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(HomePanel)
            .border(1.dp, BorderLine, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(43.dp)
                .border(4.dp, tint.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$value%",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 12.sp
            )
            Text(
                text = caption,
                color = HomeMuted,
                fontSize = 8.sp,
                lineHeight = 10.sp
            )
        }
    }
}

@Composable
fun MapNode(
    piece: String,
    name: String,
    featured: Boolean = false,
    locked: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null && !locked) Modifier.clickable(onClick = onClick) else Modifier
    Column(
        modifier = Modifier.then(clickModifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (featured) 58.dp else 38.dp)
                .clip(CircleShape)
                .background(if (featured) Color(0xFF252D1B) else Color(0xFF171E15))
                .border(
                    width = if (featured) 2.dp else 1.dp,
                    color = if (featured) HomeLime else Color(0xFF536043),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = piece,
                color = if (locked) HomeMuted else HomeLime,
                fontSize = if (featured) 31.sp else 23.sp
            )
        }
        Text(
            text = name,
            color = if (locked) HomeMuted else Color(0xFFB8C0B8),
            fontSize = 8.sp,
            textAlign = TextAlign.Center
        )
    }
}
