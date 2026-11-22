package com.knight.chesstrainer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.knight.chesstrainer.mvi.TrainerUiState
import com.knight.chesstrainer.ui.*
@Composable fun ChessTrainerPreviewContent(screen:String) {
 val state=TrainerUiState()
 Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF101711),Color(0xFF070908),Color(0xFF070908))))) { Column(Modifier.fillMaxSize().widthIn(max=500.dp).verticalScroll(rememberScrollState()).padding(horizontal=20.dp)) {
 when(screen) { "Home" -> HomeScreen({}, {}, {}); "Opening Trainer" -> OpeningTrainerScreen(state, onSquareTap = {}, onMove = { _, _ -> }, onSelectLine = {}, onSync = {}, onRestart = {}, onFeedbackReset = {}, onStockfishToggle = {}, onSkillChange = {}); "Puzzle Arena" -> PuzzleArenaScreen(state,{}, {}, {}); "Settings" -> SettingsScreen(state,{}, {}, {}, {}, {}, {}); "Sign in" -> SignInScreen(state, {}, {}, {}, {}, {}); "Sign up" -> SignUpScreen(state, {}, {}, {}, {}, {}, {}, {}); else -> error("Unknown preview screen: $screen") }; Spacer(Modifier.height(24.dp))
 } }
}
