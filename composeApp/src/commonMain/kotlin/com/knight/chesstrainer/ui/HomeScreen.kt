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
private val Night=Color(0xFF070908); private val Panel=Color(0xFF131715); private val Lime=Color(0xFFD9FF72); private val Teal=Color(0xFF63F5CE); private val Muted=Color(0xFF8A928D); private val Line=Color(0x1AFFFFFF)
@Composable
fun HomeScreen(onTrain: () -> Unit, onPuzzles: () -> Unit, onAccount: () -> Unit) {
    Header("Good evening, Alex", "Tuesday · 12 day streak", trailing = {
        Text("♘", Modifier.clip(CircleShape).background(Panel).clickable(onClick = onAccount).padding(10.dp), color = Lime, fontSize = 18.sp)
    })
    Row(
        Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0x2ED9FF72), Panel, Panel)))
            .border(1.dp, Color(0x3BD9FF72), RoundedCornerShape(20.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(62.dp).clip(RoundedCornerShape(18.dp)).background(Lime.copy(alpha = .9f)), Alignment.Center) {
            Text("♛\nIV", color = Night, textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp)
        }
        Column(Modifier.padding(start = 15.dp)) {
            Text("OPENING RANK", color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
            Text("Ruy Lopez Adept", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("1,284 Puzzle ELO   +42 this week", color = Teal, fontSize = 10.sp)
        }
    }
    SectionTitle("Daily quests", "Keep your streak alive", "⚡ 12")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuestCard("60", "Solve 5 puzzles", "3 of 5 complete", Lime, Modifier.weight(1f), onPuzzles)
        QuestCard("20", "Master 3 lines", "Ruy Lopez · 1 of 3", Teal, Modifier.weight(1f), onTrain)
    }
    SectionTitle("Opening map", "Your repertoire", "View all  ›", onTrain)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF101411))
            .border(1.dp, Line, RoundedCornerShape(20.dp)).padding(17.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            MapNode("♗", "Italian")
            MapNode("♘", "Ruy Lopez", featured = true, onClick = onTrain)
            MapNode("♙", "Queen's Gambit", locked = true)
        }
        Text("68% mastered · Exchange Variation", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
    }
    PrimaryButton("♘", "CONTINUE YOUR PATH", "Launch Training", onTrain, Modifier.padding(top = 14.dp))
    OutlinedAction("Solve a daily puzzle", onPuzzles, Modifier.padding(top = 9.dp))
}

@Composable
fun QuestCard(value: String, title: String, caption: String, tint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(modifier.clip(RoundedCornerShape(16.dp)).background(Panel).border(1.dp, Line, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(43.dp).border(4.dp, tint.copy(alpha = .6f), CircleShape), Alignment.Center) {
            Text("$value%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.padding(start = 8.dp)) {
            Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, lineHeight = 12.sp)
            Text(caption, color = Muted, fontSize = 8.sp, lineHeight = 10.sp)
        }
    }
}

@Composable
fun MapNode(piece: String, name: String, featured: Boolean = false, locked: Boolean = false, onClick: (() -> Unit)? = null) {
    Column(Modifier.then(if (onClick != null && !locked) Modifier.clickable(onClick = onClick) else Modifier), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(if (featured) 58.dp else 38.dp).clip(CircleShape)
            .background(if (featured) Color(0xFF252D1B) else Color(0xFF171E15))
            .border(if (featured) 2.dp else 1.dp, if (featured) Lime else Color(0xFF536043), CircleShape), Alignment.Center) {
            Text(piece, color = if (locked) Muted else Lime, fontSize = if (featured) 31.sp else 23.sp)
        }
        Text(name, color = if (locked) Muted else Color(0xFFB8C0B8), fontSize = 8.sp, textAlign = TextAlign.Center)
    }
}
