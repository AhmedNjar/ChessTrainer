package com.knight.chesstrainer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.rememberCoroutineScope
import com.knight.chesstrainer.db.ChessDatabase
import com.knight.chesstrainer.db.createDatabaseDriver
import com.knight.chesstrainer.auth.RestAuthGateway
import com.knight.chesstrainer.auth.createAuthHttpClient
import com.knight.chesstrainer.puzzles.PuzzleRepository
import com.knight.chesstrainer.openings.RestRepertoireGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

fun main() = application {
    val database = ChessDatabase(createDatabaseDriver())
    Window(onCloseRequest = ::exitApplication, title = "Chess Trainer") {
        val scope = rememberCoroutineScope()
        val authClient = androidx.compose.runtime.remember { createAuthHttpClient() }
        val authGateway = androidx.compose.runtime.remember { RestAuthGateway(authClient, System.getenv("CHESS_AUTH_BASE_URL") ?: "http://127.0.0.1:8080") }
        val repertoireGateway = androidx.compose.runtime.remember { RestRepertoireGateway(authClient, System.getenv("CHESS_AUTH_BASE_URL") ?: "http://127.0.0.1:8080") }
        App(database, onImportFullDatabase = { callback ->
            val dialog = FileDialog(null as Frame?, "Import Lichess puzzle CSV", FileDialog.LOAD)
            dialog.isVisible = true
            val selected = dialog.file?.let { File(dialog.directory, it) }
            if (selected == null) {
                callback(Result.failure(IllegalStateException("No file selected")))
            } else {
                scope.launch {
                    val result = runCatching {
                        withContext(Dispatchers.IO) {
                            selected.bufferedReader().use { reader -> PuzzleRepository(database).replaceFromCsv(reader.lineSequence()) }
                        }
                    }
                    callback(result)
                }
            }
        }, authGateway = authGateway, repertoireGateway = repertoireGateway)
    }
}
