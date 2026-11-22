package com.knight.chesstrainer.mvi

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import com.knight.chesstrainer.puzzles.StarterPuzzleSet
import com.knight.chesstrainer.auth.AuthCredentials
import com.knight.chesstrainer.auth.AuthUser
import com.knight.chesstrainer.openings.ChessPosition
import com.knight.chesstrainer.openings.ChessRules
import com.knight.chesstrainer.openings.OpeningCatalog
import com.knight.chesstrainer.openings.OpeningRepertoire

enum class TrainerScreen(val label: String, val icon: String, val bottomTab: Boolean = true) {
    Home("Home", "⌂"), OpeningTrainer("Train", "◎"), PuzzleArena("Puzzles", "✦"), Settings("Settings", "⚙"),
    SignIn("Sign in", "", false), SignUp("Sign up", "", false),
}
enum class MoveFeedback { Idle, Correct, Incorrect }

data class TrainerUiState(
    val screen: TrainerScreen = TrainerScreen.Home,
    val puzzleCount: Long = 10L,
    val importMessage: String = "",
    val chessPosition: ChessPosition = ChessPosition.initial,
    val selectedSquare: String? = null,
    val legalDestinations: Set<String> = emptySet(),
    val lastBoardMove: AnimatedBoardMove? = null,
    val feedback: MoveFeedback = MoveFeedback.Idle,
    val coachMessage: String = "Choose a line, then find White's highlighted repertoire move.",
    val openingLines: List<OpeningRepertoire> = OpeningCatalog.starterLines,
    val selectedOpeningId: String = OpeningCatalog.starterLines.first().id,
    val openingPlyIndex: Int = 0,
    val openingComplete: Boolean = false,
    val opponentThinking: Boolean = false,
    val repertoireSyncBusy: Boolean = false,
    val repertoireMessage: String = "Starter repertoire saved for offline training.",
    val puzzleSolved: Boolean = false,
    val puzzleIndex: Int = 0,
    val puzzleMoveIndex: Int = 0,
    val puzzlePieces: Map<String, Char> = parseFenPieces(StarterPuzzleSet.records.first().fen),
    val selectedPuzzleSquare: String? = null,
    val puzzleFeedback: String = "",
    val boardTheme: String = "Neo",
    val engineSkill: Float = 5f,
    val stockfishAvailable: Boolean = false,
    val stockfishOpponent: Boolean = false,
    val onRepertoireLine: Boolean = true,
    val stockfishMessage: String = "Stockfish is ready when you are.",
    val motionEnabled: Boolean = true,
    val authUser: AuthUser? = null,
    val authEmail: String = "",
    val authPassword: String = "",
    val authPasswordConfirmation: String = "",
    val authDisplayName: String = "",
    val authBusy: Boolean = false,
    val authError: String = "",
)

data class AnimatedBoardMove(val from: String, val to: String, val piece: Char, val id: Long)

sealed interface TrainerEvent {
    data class Navigate(val screen: TrainerScreen) : TrainerEvent
    data class TapTrainingSquare(val square: String) : TrainerEvent
    data class AttemptTrainingMove(val from: String, val to: String) : TrainerEvent
    data class SelectOpeningLine(val id: String) : TrainerEvent
    data object RequestRepertoireSync : TrainerEvent
    data class RepertoireSyncFinished(val result: Result<List<OpeningRepertoire>>) : TrainerEvent
    data class RepertoiresLoaded(val lines: List<OpeningRepertoire>) : TrainerEvent
    data class ApplyOpponentMove(val uci: String) : TrainerEvent
    data class SetStockfishAvailable(val available: Boolean) : TrainerEvent
    data class SetStockfishOpponent(val enabled: Boolean) : TrainerEvent
    data class StockfishMoveFinished(val result: Result<String>) : TrainerEvent
    data object ResetTrainingFeedback : TrainerEvent
    data class TapPuzzleSquare(val square: String) : TrainerEvent
    data object AnalyzePuzzlePosition : TrainerEvent
    data object NextPuzzle : TrainerEvent
    data class SetBoardTheme(val theme: String) : TrainerEvent
    data class SetEngineSkill(val skill: Float) : TrainerEvent
    data class SetMotion(val enabled: Boolean) : TrainerEvent
    data object RequestFullDatabaseImport : TrainerEvent
    data class ImportFinished(val result: Result<Long>) : TrainerEvent
    data class PuzzleCountLoaded(val count: Long) : TrainerEvent
    data class AuthEmailChanged(val value: String) : TrainerEvent
    data class AuthPasswordChanged(val value: String) : TrainerEvent
    data class AuthPasswordConfirmationChanged(val value: String) : TrainerEvent
    data class AuthDisplayNameChanged(val value: String) : TrainerEvent
    data object SubmitAuth : TrainerEvent
    data class AuthFinished(val result: Result<AuthUser>) : TrainerEvent
    data object SignOut : TrainerEvent
    data class SignOutFinished(val result: Result<Unit>) : TrainerEvent
}

sealed interface TrainerEffect {
    data object ChoosePuzzleCsv : TrainerEffect
    data class Authenticate(val credentials: AuthCredentials) : TrainerEffect
    data class SignOut(val accessToken: String) : TrainerEffect
    data class PlayOpponentMove(val uci: String) : TrainerEffect
    data class FindStockfishMove(val fen: String, val skillLevel: Int, val depthCap: Int) : TrainerEffect
    data object RefreshRepertoire : TrainerEffect
}

class TrainerStore(initial: TrainerUiState = TrainerUiState()) {
    private val state = MutableStateFlow(initial)
    val uiState = state.asStateFlow()
    private val effects = MutableSharedFlow<TrainerEffect>(extraBufferCapacity = 4)
    val uiEffects = effects.asSharedFlow()

    fun dispatch(event: TrainerEvent) {
        val old = state.value
        state.value = when (event) {
            is TrainerEvent.Navigate -> old.copy(
                screen = event.screen,
                authError = if (event.screen == TrainerScreen.SignIn || event.screen == TrainerScreen.SignUp) old.authError else "",
                authPassword = if (event.screen == TrainerScreen.SignIn || event.screen == TrainerScreen.SignUp) old.authPassword else "",
                authPasswordConfirmation = if (event.screen == TrainerScreen.SignIn || event.screen == TrainerScreen.SignUp) old.authPasswordConfirmation else "",
            )
            is TrainerEvent.TapTrainingSquare -> trainingTap(old, event.square)
            is TrainerEvent.AttemptTrainingMove -> attemptTrainingMove(old, event.from, event.to)
            is TrainerEvent.SelectOpeningLine -> old.openingLines.firstOrNull { it.id == event.id }?.let { line ->
                old.copy(
                    selectedOpeningId = line.id,
                    chessPosition = ChessPosition.initial,
                    selectedSquare = null,
                    legalDestinations = emptySet(),
                    lastBoardMove = null,
                    feedback = MoveFeedback.Idle,
                    coachMessage = line.summary,
                    openingPlyIndex = 0,
                    openingComplete = false,
                    opponentThinking = false,
                    onRepertoireLine = true,
                    stockfishOpponent = false,
                    stockfishMessage = "Stockfish is ready when you are.",
                )
            } ?: old
            TrainerEvent.RequestRepertoireSync -> old.copy(repertoireSyncBusy = true, repertoireMessage = "Downloading repertoire…")
                .also { effects.tryEmit(TrainerEffect.RefreshRepertoire) }
            is TrainerEvent.RepertoireSyncFinished -> event.result.fold(
                onSuccess = { lines ->
                    val merged = (old.openingLines + lines).associateBy { it.id }.values.sortedBy { it.variation }
                    old.copy(openingLines = merged, repertoireSyncBusy = false, repertoireMessage = "${lines.size} repertoire lines saved on this device for offline training.")
                },
                onFailure = { error -> old.copy(repertoireSyncBusy = false, repertoireMessage = "Offline library kept. Sync failed: ${error.message ?: "server unavailable"}") },
            )
            is TrainerEvent.RepertoiresLoaded -> if (event.lines.isEmpty()) old else old.copy(
                openingLines = (old.openingLines + event.lines).associateBy { it.id }.values.sortedBy { it.variation },
                selectedOpeningId = old.selectedOpeningId.takeIf { id -> (old.openingLines + event.lines).any { it.id == id } } ?: event.lines.first().id,
            )
            is TrainerEvent.ApplyOpponentMove -> applyOpponentMove(old, event.uci)
            is TrainerEvent.SetStockfishAvailable -> old.copy(stockfishAvailable = event.available)
            is TrainerEvent.SetStockfishOpponent -> {
                if (event.enabled && !old.stockfishAvailable) old.copy(stockfishMessage = "Stockfish is not available on this platform.")
                else old.copy(
                    stockfishOpponent = event.enabled,
                    chessPosition = ChessPosition.initial,
                    selectedSquare = null,
                    legalDestinations = emptySet(),
                    lastBoardMove = null,
                    feedback = MoveFeedback.Idle,
                    openingPlyIndex = 0,
                    openingComplete = false,
                    opponentThinking = false,
                    onRepertoireLine = true,
                    stockfishMessage = if (event.enabled) "Stockfish plays Black. The selected repertoire move is your guide." else "Guided repertoire mode.",
                    coachMessage = if (event.enabled) "Play the selected opening as White against Stockfish." else old.coachMessage,
                )
            }
            is TrainerEvent.StockfishMoveFinished -> event.result.fold(
                onSuccess = { applyStockfishMove(old, it) },
                onFailure = { error -> old.copy(opponentThinking = false, stockfishMessage = "Engine error: ${error.message ?: "could not find a move"}") },
            )
            TrainerEvent.ResetTrainingFeedback -> old.copy(feedback = MoveFeedback.Idle)
            is TrainerEvent.TapPuzzleSquare -> puzzleTap(old, event.square)
            TrainerEvent.AnalyzePuzzlePosition -> old.copy(puzzleFeedback = "Stockfish analysis is not included in this starter build.")
            TrainerEvent.NextPuzzle -> {
                val nextIndex = (old.puzzleIndex + 1) % StarterPuzzleSet.records.size
                old.copy(
                    puzzleIndex = nextIndex,
                    puzzleMoveIndex = 0,
                    puzzlePieces = parseFenPieces(StarterPuzzleSet.records[nextIndex].fen),
                    selectedPuzzleSquare = null,
                    puzzleSolved = false,
                    puzzleFeedback = "",
                    screen = TrainerScreen.PuzzleArena,
                )
            }
            is TrainerEvent.SetBoardTheme -> old.copy(boardTheme = event.theme)
            is TrainerEvent.SetEngineSkill -> old.copy(engineSkill = event.skill.coerceIn(0f, 20f))
            is TrainerEvent.SetMotion -> old.copy(motionEnabled = event.enabled)
            TrainerEvent.RequestFullDatabaseImport -> old.copy(importMessage = "Choose lichess_db_puzzle.csv to begin importing").also { effects.tryEmit(TrainerEffect.ChoosePuzzleCsv) }
            is TrainerEvent.ImportFinished -> event.result.fold(
                onSuccess = { count -> old.copy(puzzleCount = count, importMessage = "Imported $count puzzles for offline play.") },
                onFailure = { error -> old.copy(importMessage = "Import failed: ${error.message ?: "unknown error"}") },
            )
            is TrainerEvent.PuzzleCountLoaded -> old.copy(puzzleCount = event.count)
            is TrainerEvent.AuthEmailChanged -> old.copy(authEmail = event.value, authError = "")
            is TrainerEvent.AuthPasswordChanged -> old.copy(authPassword = event.value, authError = "")
            is TrainerEvent.AuthPasswordConfirmationChanged -> old.copy(authPasswordConfirmation = event.value, authError = "")
            is TrainerEvent.AuthDisplayNameChanged -> old.copy(authDisplayName = event.value, authError = "")
            TrainerEvent.SubmitAuth -> submitAuth(old)
            is TrainerEvent.AuthFinished -> event.result.fold(
                onSuccess = { user -> old.copy(authUser = user, authBusy = false, authError = "", authPassword = "", authPasswordConfirmation = "", screen = TrainerScreen.Settings) },
                onFailure = { error -> old.copy(authBusy = false, authError = error.message ?: "Could not sign in. Try again.") },
            )
            TrainerEvent.SignOut -> old.authUser?.let { user ->
                old.copy(authBusy = true, authError = "").also { effects.tryEmit(TrainerEffect.SignOut(user.accessToken)) }
            } ?: old
            is TrainerEvent.SignOutFinished -> event.result.fold(
                onSuccess = { old.copy(authUser = null, authBusy = false, authError = "", authPassword = "", screen = TrainerScreen.Settings) },
                onFailure = { error -> old.copy(authBusy = false, authError = error.message ?: "Could not sign out.") },
            )
        }
    }

    private fun submitAuth(old: TrainerUiState): TrainerUiState {
        if (old.authBusy) return old
        val email = old.authEmail.trim().lowercase()
        val signUp = old.screen == TrainerScreen.SignUp
        val error = when {
            !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email) -> "Enter a valid email address."
            old.authPassword.length < 8 -> "Password must be at least 8 characters."
            signUp && old.authDisplayName.isBlank() -> "Enter your display name."
            signUp && old.authPassword != old.authPasswordConfirmation -> "Passwords do not match."
            else -> ""
        }
        if (error.isNotEmpty()) return old.copy(authError = error)
        return old.copy(authBusy = true, authError = "").also {
            effects.tryEmit(TrainerEffect.Authenticate(AuthCredentials(email, old.authPassword, old.authDisplayName.takeIf { signUp }, signUp)))
        }
    }

    private fun trainingTap(old: TrainerUiState, square: String): TrainerUiState {
        if (old.opponentThinking || old.openingComplete && !old.stockfishOpponent) return old
        val selected = old.selectedSquare
        if (selected == null || (old.chessPosition.pieces[square]?.isUpperCase() == true && square !in old.legalDestinations)) {
            val piece = old.chessPosition.pieces[square]
            if (piece?.isUpperCase() != true) return old.copy(selectedSquare = null, legalDestinations = emptySet())
            val moves = ChessRules.legalMoves(old.chessPosition, square).map { it.to }.toSet()
            return old.copy(selectedSquare = square, legalDestinations = moves, feedback = MoveFeedback.Idle)
        }
        if (square == selected) return old.copy(selectedSquare = null, legalDestinations = emptySet())
        return attemptTrainingMove(old, selected, square)
    }

    private fun attemptTrainingMove(old: TrainerUiState, from: String, to: String): TrainerUiState {
        if (old.opponentThinking || old.openingComplete && !old.stockfishOpponent) return old
        val legal = ChessRules.legalMoves(old.chessPosition, from).firstOrNull { it.to == to } ?: return old.copy(
            selectedSquare = null, legalDestinations = emptySet(), feedback = MoveFeedback.Incorrect,
            coachMessage = "That square is not a legal move for this piece. Choose one of the highlighted squares.",
        )
        val line = old.openingLines.firstOrNull { it.id == old.selectedOpeningId } ?: return old
        val expected = line.plies.getOrNull(old.openingPlyIndex).takeIf { old.onRepertoireLine }
        if (expected == null && !old.stockfishOpponent) return old.copy(openingComplete = true, selectedSquare = null, legalDestinations = emptySet(), feedback = MoveFeedback.Correct, coachMessage = "Line complete. Great work!")
        if (!old.stockfishOpponent && expected != null && legal.uci.take(4) != expected.uci.take(4)) return old.copy(
            selectedSquare = null, legalDestinations = emptySet(), feedback = MoveFeedback.Incorrect,
            coachMessage = "Legal move, but this line calls for ${expected.san}. ${expected.guide}".trim(),
        )
        val nextPosition = if (old.stockfishOpponent) ChessRules.apply(old.chessPosition, legal)
            else expected?.let { ChessRules.playUci(old.chessPosition, it.uci) }
                ?: return old.copy(
            selectedSquare = null, legalDestinations = emptySet(), feedback = MoveFeedback.Incorrect,
            coachMessage = "This repertoire move is not legal from the current position. Select another line or sync the library.",
        )
        val staysOnLine = old.onRepertoireLine && expected?.uci?.take(4) == legal.uci.take(4)
        val nextIndex = old.openingPlyIndex + if (staysOnLine) 1 else 0
        val reply = line.plies.getOrNull(nextIndex)?.takeIf { !old.stockfishOpponent && staysOnLine && !nextPosition.whiteToMove }
        val id = (old.lastBoardMove?.id ?: 0L) + 1L
        val updated = old.copy(
            chessPosition = nextPosition,
            selectedSquare = null,
            legalDestinations = emptySet(),
            lastBoardMove = AnimatedBoardMove(from, to, old.chessPosition.pieces[from] ?: 'P', id),
            feedback = MoveFeedback.Correct,
            openingPlyIndex = nextIndex,
            openingComplete = !old.stockfishOpponent && reply == null && nextIndex >= line.plies.size,
            opponentThinking = old.stockfishOpponent || reply != null,
            onRepertoireLine = staysOnLine,
            stockfishMessage = if (old.stockfishOpponent) "Stockfish is thinking…" else old.stockfishMessage,
            coachMessage = if (old.stockfishOpponent && !staysOnLine) "Free play: the move left this prepared line. Keep playing against Stockfish." else expected?.guide?.ifBlank { "Correct. ${expected.san} is the repertoire move." } ?: "Legal move.",
        )
        return updated.also {
            if (old.stockfishOpponent) effects.tryEmit(TrainerEffect.FindStockfishMove(ChessRules.toFen(nextPosition), old.engineSkill.toInt(), depthForSkill(old.engineSkill.toInt())))
            else if (reply != null) effects.tryEmit(TrainerEffect.PlayOpponentMove(reply.uci))
        }
    }

    private fun applyStockfishMove(old: TrainerUiState, uci: String): TrainerUiState {
        val move = ChessRules.legalMoves(old.chessPosition).firstOrNull { it.uci == uci }
            ?: return old.copy(opponentThinking = false, stockfishMessage = "Stockfish returned an invalid move: $uci")
        val position = ChessRules.apply(old.chessPosition, move)
        val line = old.openingLines.firstOrNull { it.id == old.selectedOpeningId }
        val expected = line?.plies?.getOrNull(old.openingPlyIndex)
        val staysOnLine = old.onRepertoireLine && expected?.uci?.take(4) == move.uci.take(4)
        val nextIndex = old.openingPlyIndex + if (staysOnLine) 1 else 0
        val id = (old.lastBoardMove?.id ?: 0L) + 1L
        return old.copy(
            chessPosition = position,
            openingPlyIndex = nextIndex,
            opponentThinking = false,
            onRepertoireLine = staysOnLine,
            stockfishMessage = "Stockfish · Skill ${old.engineSkill.toInt()} · ready",
            coachMessage = if (staysOnLine) "${line?.plies?.getOrNull(nextIndex)?.guide.orEmpty()} Your move as White." else "Stockfish played $uci. Continue with any legal move.",
            lastBoardMove = AnimatedBoardMove(move.from, move.to, old.chessPosition.pieces[move.from] ?: 'p', id),
        )
    }

    private fun depthForSkill(skill: Int): Int = when {
        skill < 5 -> 4
        skill < 10 -> 7
        skill < 15 -> 10
        else -> 14
    }

    private fun applyOpponentMove(old: TrainerUiState, uci: String): TrainerUiState {
        val line = old.openingLines.firstOrNull { it.id == old.selectedOpeningId } ?: return old.copy(opponentThinking = false)
        val move = ChessRules.legalMoves(old.chessPosition).firstOrNull { it.uci == uci }
            ?: return old.copy(opponentThinking = false, coachMessage = "The saved opponent reply is not legal. Please sync the repertoire.")
        val position = ChessRules.apply(old.chessPosition, move)
        val nextIndex = old.openingPlyIndex + 1
        val complete = nextIndex >= line.plies.size
        val id = (old.lastBoardMove?.id ?: 0L) + 1L
        return old.copy(
            chessPosition = position,
            openingPlyIndex = nextIndex,
            openingComplete = complete,
            opponentThinking = false,
            feedback = if (complete) MoveFeedback.Correct else old.feedback,
            coachMessage = if (complete) "Line complete. Great work!" else "${line.plies.getOrNull(nextIndex)?.guide.orEmpty()} Your move as White.",
            lastBoardMove = AnimatedBoardMove(move.from, move.to, old.chessPosition.pieces[move.from] ?: 'p', id),
        )
    }

    private fun puzzleTap(old: TrainerUiState, square: String): TrainerUiState {
        if (old.puzzleSolved) return old
        val puzzle = StarterPuzzleSet.records[old.puzzleIndex]
        val moves = puzzle.moves.split(' ').filter(String::isNotBlank)
        val expected = moves.getOrNull(old.puzzleMoveIndex) ?: return old
        val selected = old.selectedPuzzleSquare
        if (selected == null) {
            return if (square == expected.take(2)) old.copy(selectedPuzzleSquare = square, puzzleFeedback = "")
            else if (old.puzzlePieces.containsKey(square)) old.copy(selectedPuzzleSquare = square, puzzleFeedback = "Choose the piece that starts the solution.")
            else old
        }
        if (square != expected.substring(2, 4)) {
            return old.copy(selectedPuzzleSquare = null, puzzleFeedback = "Not the solution. Try another move.")
        }

        val updated = applyUciMove(old.puzzlePieces, expected)
        var nextIndex = old.puzzleMoveIndex + 1
        // Lichess solutions alternate between the player's move and the forced reply.
        moves.getOrNull(nextIndex)?.let { reply ->
            updated.putAll(applyUciMove(updated, reply))
            nextIndex++
        }
        return old.copy(
            puzzlePieces = updated,
            puzzleMoveIndex = nextIndex,
            selectedPuzzleSquare = null,
            puzzleSolved = nextIndex >= moves.size,
            puzzleFeedback = if (nextIndex >= moves.size) "Puzzle solved!" else "Correct. Find the next move.",
        )
    }
}

private fun applyUciMove(position: Map<String, Char>, move: String): MutableMap<String, Char> =
    position.toMutableMap().apply {
        val piece = remove(move.take(2)) ?: return@apply
        put(move.substring(2, 4), piece)
    }

private fun parseFenPieces(fen: String): Map<String, Char> {
    val ranks = fen.substringBefore(' ').split('/')
    val pieces = mutableMapOf<String, Char>()
    ranks.forEachIndexed { row, rank ->
        var file = 0
        rank.forEach { symbol ->
            if (symbol.isDigit()) file += symbol.digitToInt()
            else {
                if (file in 0..7) pieces["${'a' + file}${8 - row}"] = symbol
                file++
            }
        }
    }
    return pieces
}

private fun openingPosition() = mapOf(
    "a8" to 'r', "b8" to 'n', "c8" to 'b', "d8" to 'q', "e8" to 'k', "f8" to 'b', "h8" to 'r',
    "a7" to 'p', "b7" to 'p', "c7" to 'p', "d7" to 'p', "e7" to 'p', "f7" to 'p', "g7" to 'p', "h7" to 'p',
    "e5" to 'p', "c6" to 'n', "b5" to 'B', "e4" to 'P', "a2" to 'P', "b2" to 'P', "c2" to 'P', "d2" to 'P', "e2" to 'P', "f2" to 'P', "g2" to 'P', "h2" to 'P',
    "a1" to 'R', "b1" to 'N', "c1" to 'B', "d1" to 'Q', "e1" to 'K', "f1" to 'B', "g1" to 'N', "h1" to 'R',
)
