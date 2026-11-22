package com.knight.chesstrainer.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
fun SettingsScreen(state:TrainerUiState,onThemeChange:(String)->Unit,onSkillChange:(Float)->Unit,onMotionChange:(Boolean)->Unit,onImport:()->Unit,onAuth:()->Unit,onSignOut:()->Unit) {
    Header("Make it yours","Settings")
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Color(0xFF131715)).clickable { if(state.authUser == null) onAuth() else onSignOut() }.padding(14.dp),verticalAlignment=Alignment.CenterVertically) { Box(Modifier.size(46.dp).clip(CircleShape).background(Color(0xFFD9FF72)),Alignment.Center) { Text(state.authUser?.displayName?.take(2)?.uppercase() ?: "AK",color=Color(0xFF070908),fontWeight=FontWeight.Bold) }; Column(Modifier.weight(1f).padding(horizontal=12.dp)) { Text(state.authUser?.displayName ?: "Guest player",color=Color.White,fontSize=13.sp,fontWeight=FontWeight.SemiBold); Text(state.authUser?.email ?: "Offline player · tap to sign in",color=Color(0xFF8A928D),fontSize=9.sp) }; Text(if(state.authUser == null) "Sign in" else "Sign out",color=Color(0xFFD9FF72),fontSize=10.sp) }
    SectionTitle("Board & pieces","Choose your training arena")
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("Modern","Wood","Neo").forEach { name -> Column(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(if(state.boardTheme==name) Color(0x14D9FF72) else Color(0xFF131715)).border(1.dp,if(state.boardTheme==name) Color(0xFFD9FF72) else Color(0x1AFFFFFF),RoundedCornerShape(13.dp)).clickable { onThemeChange(name) }.padding(9.dp),horizontalAlignment=Alignment.CenterHorizontally) { Text("♞",color=Color(0xFFD9FF72),fontSize=31.sp); Text(name,color=Color.White,fontSize=9.sp) } } }
    SectionTitle("Engine","Strength preference · ${state.engineSkill.toInt()}"); Slider(value=state.engineSkill,onValueChange=onSkillChange,valueRange=0f..20f,colors=SliderDefaults.colors(thumbColor=Color(0xFFD9FF72),activeTrackColor=Color(0xFFD9FF72),inactiveTrackColor=Color(0xFF3A403B),activeTickColor=Color(0xFF070908),inactiveTickColor=Color(0xFF3A403B)))
    Text(if(state.stockfishAvailable) "Used for local Stockfish matches. Lower levels intentionally choose weaker moves." else "Stockfish match support is unavailable on this device.",Modifier.padding(top=2.dp),color=Color(0xFF8A928D),fontSize=9.sp)
    SectionTitle("Experience","Animation and feedback")
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF131715)).clickable { onMotionChange(!state.motionEnabled) }.padding(14.dp),verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Piece animations",color=Color.White,fontSize=11.sp); Text(if(state.motionEnabled) "Full motion" else "Reduced motion",color=Color(0xFF8A928D),fontSize=9.sp) }; Switch(checked=state.motionEnabled,onCheckedChange=onMotionChange,colors=SwitchDefaults.colors(checkedThumbColor=Color(0xFF070908),checkedTrackColor=Color(0xFFD9FF72))) }
    SectionTitle("Offline library","${state.puzzleCount} puzzles ready"); OutlinedAction("Import full Lichess CSV",onImport)
    if(state.importMessage.isNotBlank()) Text(state.importMessage,Modifier.fillMaxWidth().padding(top=8.dp),color=if(state.importMessage.startsWith("Import failed")) Color(0xFFFF796F) else Color(0xFF8A928D),fontSize=9.sp)
    if(state.authError.isNotBlank()) Text(state.authError,Modifier.fillMaxWidth().padding(top=8.dp),color=Color(0xFFFF796F),fontSize=9.sp)
}
