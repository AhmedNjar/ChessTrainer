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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knight.chesstrainer.auth.AuthGateway
import com.knight.chesstrainer.db.ChessDatabase
import com.knight.chesstrainer.engine.StockfishGateway
import com.knight.chesstrainer.mvi.*
import com.knight.chesstrainer.openings.OpeningRepository
import com.knight.chesstrainer.openings.RepertoireGateway
import com.knight.chesstrainer.puzzles.PuzzleRepository
import com.knight.chesstrainer.puzzles.StarterPuzzleSet
import com.knight.chesstrainer.ui.*
import kotlinx.coroutines.delay

typealias FullDatabaseImportRequest = ((Result<Long>) -> Unit) -> Unit

private val BackgroundNight = Color(0xFF070908)
private val TextMuted = Color(0xFF8A928D)
private val BorderLine = Color(0x1AFFFFFF)

@Composable
fun App(
    database: ChessDatabase? = null,
    onImportFullDatabase: FullDatabaseImportRequest? = null,
    authGateway: AuthGateway? = null,
    repertoireGateway: RepertoireGateway? = null,
    stockfishGateway: StockfishGateway? = null,
) {
    val store = remember {
        TrainerStore(
            TrainerUiState(
                puzzleCount = StarterPuzzleSet.records.size.toLong(),
                stockfishAvailable = stockfishGateway?.available == true,
            )
        )
    }
    val state by store.uiState.collectAsState()

    LaunchedEffect(database) {
        if (database != null) {
            val count = PuzzleRepository(database).seedStarterIfEmpty()
            store.dispatch(TrainerEvent.PuzzleCountLoaded(count))
            val catalog = OpeningRepository(database).updateOfflineCatalog()
            store.dispatch(TrainerEvent.RepertoiresLoaded(catalog))
        }
    }

    LaunchedEffect(store, onImportFullDatabase, authGateway, repertoireGateway, database, stockfishGateway) {
        store.uiEffects.collect { effect ->
            when (effect) {
                TrainerEffect.ChoosePuzzleCsv -> {
                    if (onImportFullDatabase == null) {
                        store.dispatch(
                            TrainerEvent.ImportFinished(
                                Result.failure(IllegalStateException("CSV import is not supported on this platform."))
                            )
                        )
                    } else {
                        onImportFullDatabase { result ->
                            store.dispatch(TrainerEvent.ImportFinished(result))
                        }
                    }
                }
                is TrainerEffect.Authenticate -> {
                    val result = if (authGateway == null) {
                        Result.failure(IllegalStateException("Authentication server is not configured."))
                    } else {
                        runCatching { authGateway.authenticate(effect.credentials) }
                    }
                    store.dispatch(TrainerEvent.AuthFinished(result))
                }
                is TrainerEffect.SignOut -> {
                    val result = if (authGateway == null) {
                        Result.failure(IllegalStateException("Authentication server is not configured."))
                    } else {
                        runCatching { authGateway.signOut(effect.accessToken) }
                    }
                    store.dispatch(TrainerEvent.SignOutFinished(result))
                }
                is TrainerEffect.PlayOpponentMove -> {
                    delay(420)
                    store.dispatch(TrainerEvent.ApplyOpponentMove(effect.uci))
                }
                is TrainerEffect.FindStockfishMove -> {
                    val result = if (stockfishGateway == null) {
                        Result.failure(IllegalStateException("Stockfish engine is unavailable."))
                    } else {
                        runCatching {
                            stockfishGateway.bestMove(effect.fen, effect.skillLevel, effect.depthCap)
                        }
                    }
                    store.dispatch(TrainerEvent.StockfishMoveFinished(result))
                }
                TrainerEffect.RefreshRepertoire -> {
                    val result = if (repertoireGateway == null) {
                        Result.failure(IllegalStateException("Repertoire server is not configured."))
                    } else {
                        runCatching { repertoireGateway.fetchRepertoires() }
                    }
                    result.onSuccess { lines ->
                        database?.let { OpeningRepository(it).save(lines) }
                    }
                    store.dispatch(TrainerEvent.RepertoireSyncFinished(result))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF101711), BackgroundNight, BackgroundNight)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 500.dp)
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                key(state.screen) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        when (state.screen) {
                            TrainerScreen.Home -> HomeScreen(
                                onTrain = { store.dispatch(TrainerEvent.Navigate(TrainerScreen.OpeningTrainer)) },
                                onPuzzles = { store.dispatch(TrainerEvent.Navigate(TrainerScreen.PuzzleArena)) },
                                onAccount = {
                                    val target = if (state.authUser == null) TrainerScreen.SignIn else TrainerScreen.Settings
                                    store.dispatch(TrainerEvent.Navigate(target))
                                }
                            )
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
                            TrainerScreen.PuzzleArena -> PuzzleArenaScreen(
                                state = state,
                                onSquareTap = { store.dispatch(TrainerEvent.TapPuzzleSquare(it)) },
                                onNext = { store.dispatch(TrainerEvent.NextPuzzle) },
                                onAnalyze = { store.dispatch(TrainerEvent.AnalyzePuzzlePosition) }
                            )
                            TrainerScreen.Settings -> SettingsScreen(
                                state = state,
                                onThemeChange = { store.dispatch(TrainerEvent.SetBoardTheme(it)) },
                                onSkillChange = { store.dispatch(TrainerEvent.SetEngineSkill(it)) },
                                onMotionChange = { store.dispatch(TrainerEvent.SetMotion(it)) },
                                onImport = { store.dispatch(TrainerEvent.RequestFullDatabaseImport) },
                                onAuth = { store.dispatch(TrainerEvent.Navigate(TrainerScreen.SignIn)) },
                                onSignOut = { store.dispatch(TrainerEvent.SignOut) }
                            )
                            TrainerScreen.SignIn -> SignInScreen(
                                state = state,
                                onEmail = { store.dispatch(TrainerEvent.AuthEmailChanged(it)) },
                                onPassword = { store.dispatch(TrainerEvent.AuthPasswordChanged(it)) },
                                onSubmit = { store.dispatch(TrainerEvent.SubmitAuth) },
                                onSignUp = { store.dispatch(TrainerEvent.Navigate(TrainerScreen.SignUp)) },
                                onBack = { store.dispatch(TrainerEvent.Navigate(TrainerScreen.Settings)) }
                            )
                            TrainerScreen.SignUp -> SignUpScreen(
                                state = state,
                                onName = { store.dispatch(TrainerEvent.AuthDisplayNameChanged(it)) },
                                onEmail = { store.dispatch(TrainerEvent.AuthEmailChanged(it)) },
                                onPassword = { store.dispatch(TrainerEvent.AuthPasswordChanged(it)) },
                                onConfirmation = { store.dispatch(TrainerEvent.AuthPasswordConfirmationChanged(it)) },
                                onSubmit = { store.dispatch(TrainerEvent.SubmitAuth) },
                                onSignIn = { store.dispatch(TrainerEvent.Navigate(TrainerScreen.SignIn)) },
                                onBack = { store.dispatch(TrainerEvent.Navigate(TrainerScreen.Settings)) }
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            if (state.screen.bottomTab) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp)
                        .background(Color(0xF20A0D0B))
                        .border(1.dp, BorderLine),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrainerScreen.entries
                        .filter { it.bottomTab }
                        .forEach { item ->
                            val active = state.screen == item
                            Column(
                                modifier = Modifier
                                    .width(70.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { store.dispatch(TrainerEvent.Navigate(item)) }
                                    .padding(vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = item.icon,
                                    color = if (active) Color(0xFFD9FF72) else TextMuted,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = item.label,
                                    color = if (active) Color(0xFFD9FF72) else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                }
            }
        }
    }
}
