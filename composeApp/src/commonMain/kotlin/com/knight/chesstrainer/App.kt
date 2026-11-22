package com.knight.chesstrainer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knight.chesstrainer.db.ChessDatabase
import com.knight.chesstrainer.auth.AuthGateway
import com.knight.chesstrainer.engine.StockfishGateway
import com.knight.chesstrainer.openings.OpeningRepository
import com.knight.chesstrainer.openings.RepertoireGateway
import com.knight.chesstrainer.mvi.*
import com.knight.chesstrainer.puzzles.PuzzleRepository
import com.knight.chesstrainer.puzzles.StarterPuzzleSet
import com.knight.chesstrainer.ui.*
import kotlinx.coroutines.delay

/** Callback supplied by the Android/Desktop host to open a platform file picker. */
typealias FullDatabaseImportRequest = ((Result<Long>) -> Unit) -> Unit
private val Night=Color(0xFF070908)
private val Muted=Color(0xFF8A928D)
private val Line=Color(0x1AFFFFFF)

@Composable
fun App(database:ChessDatabase?=null,onImportFullDatabase:FullDatabaseImportRequest?=null,authGateway:AuthGateway?=null,repertoireGateway:RepertoireGateway?=null,stockfishGateway:StockfishGateway?=null) {
    val store= remember { TrainerStore(TrainerUiState(puzzleCount=StarterPuzzleSet.records.size.toLong(),stockfishAvailable=stockfishGateway?.available==true)) }
    val state by store.uiState.collectAsState()
    LaunchedEffect(database) {
        if(database!=null) {
            store.dispatch(TrainerEvent.PuzzleCountLoaded(PuzzleRepository(database).seedStarterIfEmpty()))
            store.dispatch(TrainerEvent.RepertoiresLoaded(OpeningRepository(database).updateOfflineCatalog()))
        }
    }
    LaunchedEffect(store,onImportFullDatabase,authGateway,repertoireGateway,database,stockfishGateway) {
        store.uiEffects.collect { effect -> when(effect) {
            TrainerEffect.ChoosePuzzleCsv -> if(onImportFullDatabase==null) store.dispatch(TrainerEvent.ImportFinished(Result.failure(IllegalStateException("Choose a CSV import from a supported platform.")))) else onImportFullDatabase { result -> store.dispatch(TrainerEvent.ImportFinished(result)) }
            is TrainerEffect.Authenticate -> {
                val result = if (authGateway == null) Result.failure(IllegalStateException("Authentication backend is not configured."))
                    else runCatching { authGateway.authenticate(effect.credentials) }
                store.dispatch(TrainerEvent.AuthFinished(result))
            }
            is TrainerEffect.SignOut -> {
                val result = if (authGateway == null) Result.failure(IllegalStateException("Authentication backend is not configured."))
                    else runCatching { authGateway.signOut(effect.accessToken) }
                store.dispatch(TrainerEvent.SignOutFinished(result))
            }
            is TrainerEffect.PlayOpponentMove -> {
                delay(420)
                store.dispatch(TrainerEvent.ApplyOpponentMove(effect.uci))
            }
            is TrainerEffect.FindStockfishMove -> {
                val result = if(stockfishGateway==null) Result.failure(IllegalStateException("Stockfish is not configured on this platform."))
                    else runCatching { stockfishGateway.bestMove(effect.fen,effect.skillLevel,effect.depthCap) }
                store.dispatch(TrainerEvent.StockfishMoveFinished(result))
            }
            TrainerEffect.RefreshRepertoire -> {
                val result = if (repertoireGateway == null) Result.failure(IllegalStateException("Repertoire server is not configured."))
                    else runCatching { repertoireGateway.fetchRepertoires() }
                result.onSuccess { lines -> database?.let { OpeningRepository(it).save(lines) } }
                store.dispatch(TrainerEvent.RepertoireSyncFinished(result))
            }
        } }
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF101711),Night,Night)))) {
        Column(Modifier.fillMaxSize().widthIn(max=500.dp).align(Alignment.TopCenter)) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                key(state.screen) { Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp)) {
                    when(state.screen) {
                        TrainerScreen.Home -> HomeScreen(onTrain={store.dispatch(TrainerEvent.Navigate(TrainerScreen.OpeningTrainer))},onPuzzles={store.dispatch(TrainerEvent.Navigate(TrainerScreen.PuzzleArena))},onAccount={store.dispatch(TrainerEvent.Navigate(if(state.authUser == null) TrainerScreen.SignIn else TrainerScreen.Settings))})
                        TrainerScreen.OpeningTrainer -> OpeningTrainerScreen(
                            state = state,
                            onSquareTap = { store.dispatch(TrainerEvent.TapTrainingSquare(it)) },
                            onMove = { from, to -> store.dispatch(TrainerEvent.AttemptTrainingMove(from, to)) },
                            onSelectLine = { store.dispatch(TrainerEvent.SelectOpeningLine(it)) },
                            onSync = { store.dispatch(TrainerEvent.RequestRepertoireSync) },
                            onRestart = { store.dispatch(TrainerEvent.SelectOpeningLine(state.selectedOpeningId)) },
                            onFeedbackReset = { store.dispatch(TrainerEvent.ResetTrainingFeedback) },
                            onStockfishToggle = { store.dispatch(TrainerEvent.SetStockfishOpponent(it)) },
                            onSkillChange = { store.dispatch(TrainerEvent.SetEngineSkill(it)) },
                        )
                        TrainerScreen.PuzzleArena -> PuzzleArenaScreen(state,{store.dispatch(TrainerEvent.TapPuzzleSquare(it))},{store.dispatch(TrainerEvent.NextPuzzle)},{store.dispatch(TrainerEvent.AnalyzePuzzlePosition)})
                        TrainerScreen.Settings -> SettingsScreen(state,{store.dispatch(TrainerEvent.SetBoardTheme(it))},{store.dispatch(TrainerEvent.SetEngineSkill(it))},{store.dispatch(TrainerEvent.SetMotion(it))},{store.dispatch(TrainerEvent.RequestFullDatabaseImport)},{store.dispatch(TrainerEvent.Navigate(TrainerScreen.SignIn))},{store.dispatch(TrainerEvent.SignOut)})
                        TrainerScreen.SignIn -> SignInScreen(state,{store.dispatch(TrainerEvent.AuthEmailChanged(it))},{store.dispatch(TrainerEvent.AuthPasswordChanged(it))},{store.dispatch(TrainerEvent.SubmitAuth)},{store.dispatch(TrainerEvent.Navigate(TrainerScreen.SignUp))},{store.dispatch(TrainerEvent.Navigate(TrainerScreen.Settings))})
                        TrainerScreen.SignUp -> SignUpScreen(state,{store.dispatch(TrainerEvent.AuthDisplayNameChanged(it))},{store.dispatch(TrainerEvent.AuthEmailChanged(it))},{store.dispatch(TrainerEvent.AuthPasswordChanged(it))},{store.dispatch(TrainerEvent.AuthPasswordConfirmationChanged(it))},{store.dispatch(TrainerEvent.SubmitAuth)},{store.dispatch(TrainerEvent.Navigate(TrainerScreen.SignIn))},{store.dispatch(TrainerEvent.Navigate(TrainerScreen.Settings))})
                    }
                    Spacer(Modifier.height(24.dp))
                } }
            }
            if (state.screen.bottomTab) Row(Modifier.fillMaxWidth().height(76.dp).background(Color(0xF20A0D0B)).border(1.dp,Line),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically) {
                TrainerScreen.entries.filter { it.bottomTab }.forEach { item -> val active=state.screen==item; Column(Modifier.width(70.dp).clip(RoundedCornerShape(16.dp)).clickable { store.dispatch(TrainerEvent.Navigate(item)) }.padding(vertical=5.dp),horizontalAlignment=Alignment.CenterHorizontally) { Text(item.icon,color=if(active) Color(0xFFD9FF72) else Muted,fontSize=20.sp); Text(item.label,color=if(active) Color(0xFFD9FF72) else Muted,fontSize=10.sp,fontWeight=FontWeight.SemiBold) } }
            }
        }
    }
}
