package com.knight.chesstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knight.chesstrainer.mvi.TrainerUiState

@Composable
fun SettingsScreen(
    state: TrainerUiState,
    onThemeChange: (String) -> Unit,
    onSkillChange: (Float) -> Unit,
    onMotionChange: (Boolean) -> Unit,
    onImport: () -> Unit,
    onAuth: () -> Unit,
    onSignOut: () -> Unit,
) {
    Header(title = "Make it yours", eyebrow = "Settings")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(Color(0xFF131715))
            .clickable { if (state.authUser == null) onAuth() else onSignOut() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFD9FF72)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.authUser?.displayName?.take(2)?.uppercase() ?: "AK",
                color = Color(0xFF070908),
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = state.authUser?.displayName ?: "Guest player",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = state.authUser?.email ?: "Offline player · tap to sign in",
                color = Color(0xFF8A928D),
                fontSize = 9.sp
            )
        }
        Text(
            text = if (state.authUser == null) "Sign in" else "Sign out",
            color = Color(0xFFD9FF72),
            fontSize = 10.sp
        )
    }

    SectionTitle(eyebrow = "Board & pieces", title = "Choose your training arena")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Modern", "Wood", "Neo").forEach { name ->
            val isSelected = state.boardTheme == name
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (isSelected) Color(0x14D9FF72) else Color(0xFF131715))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color(0xFFD9FF72) else Color(0x1AFFFFFF),
                        shape = RoundedCornerShape(13.dp)
                    )
                    .clickable { onThemeChange(name) }
                    .padding(9.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "♞",
                    color = Color(0xFFD9FF72),
                    fontSize = 31.sp
                )
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 9.sp
                )
            }
        }
    }

    SectionTitle(eyebrow = "Engine", title = "Strength preference · ${state.engineSkill.toInt()}")
    Slider(
        value = state.engineSkill,
        onValueChange = onSkillChange,
        valueRange = 0f..20f,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFFD9FF72),
            activeTrackColor = Color(0xFFD9FF72),
            inactiveTrackColor = Color(0xFF3A403B),
            activeTickColor = Color(0xFF070908),
            inactiveTickColor = Color(0xFF3A403B)
        )
    )
    Text(
        text = if (state.stockfishAvailable) {
            "Used for local Stockfish matches. Lower levels intentionally choose weaker moves."
        } else {
            "Stockfish match support is unavailable on this device."
        },
        modifier = Modifier.padding(top = 2.dp),
        color = Color(0xFF8A928D),
        fontSize = 9.sp
    )

    SectionTitle(eyebrow = "Experience", title = "Animation and feedback")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF131715))
            .clickable { onMotionChange(!state.motionEnabled) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Piece animations",
                color = Color.White,
                fontSize = 11.sp
            )
            Text(
                text = if (state.motionEnabled) "Full motion" else "Reduced motion",
                color = Color(0xFF8A928D),
                fontSize = 9.sp
            )
        }
        Switch(
            checked = state.motionEnabled,
            onCheckedChange = onMotionChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF070908),
                checkedTrackColor = Color(0xFFD9FF72)
            )
        )
    }

    SectionTitle(eyebrow = "Offline library", title = "${state.puzzleCount} puzzles ready")
    OutlinedAction(label = "Import full Lichess CSV", onClick = onImport)

    if (state.importMessage.isNotBlank()) {
        val isError = state.importMessage.startsWith("Import failed")
        Text(
            text = state.importMessage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = if (isError) Color(0xFFFF796F) else Color(0xFF8A928D),
            fontSize = 9.sp
        )
    }
    if (state.authError.isNotBlank()) {
        Text(
            text = state.authError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = Color(0xFFFF796F),
            fontSize = 9.sp
        )
    }
}
