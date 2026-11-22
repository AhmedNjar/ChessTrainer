package com.knight.chesstrainer.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.knight.chesstrainer.App
import com.knight.chesstrainer.auth.RestAuthGateway
import com.knight.chesstrainer.auth.createAuthHttpClient
import com.knight.chesstrainer.engine.AndroidStockfishGateway
import com.knight.chesstrainer.db.ChessDatabase
import com.knight.chesstrainer.db.createDatabaseDriver
import com.knight.chesstrainer.openings.RestRepertoireGateway
import com.knight.chesstrainer.puzzles.PuzzleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val importScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var database: ChessDatabase
    private lateinit var stockfishGateway: AndroidStockfishGateway
    private val authHttpClient by lazy { createAuthHttpClient() }
    private var pendingImport: ((Result<Long>) -> Unit)? = null

    private val selectPuzzleCsv = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val callback = pendingImport.also { pendingImport = null } ?: return@registerForActivityResult
        if (uri == null) return@registerForActivityResult
        importScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val stream = contentResolver.openInputStream(uri) ?: error("Could not open the selected file")
                    stream.bufferedReader().use { reader -> PuzzleRepository(database).replaceFromCsv(reader.lineSequence()) }
                }
            }
            callback(result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = ChessDatabase(createDatabaseDriver(this))
        stockfishGateway = AndroidStockfishGateway(applicationContext)
        window.statusBarColor = android.graphics.Color.rgb(7, 9, 8)
        window.navigationBarColor = android.graphics.Color.rgb(7, 9, 8)
        setContent {
            App(database, onImportFullDatabase = { callback ->
                pendingImport = callback
                selectPuzzleCsv.launch(arrayOf("text/csv", "text/plain", "application/octet-stream"))
            },
                authGateway = RestAuthGateway(authHttpClient, BuildConfig.AUTH_BASE_URL),
                repertoireGateway = RestRepertoireGateway(authHttpClient, BuildConfig.AUTH_BASE_URL),
                stockfishGateway = stockfishGateway,
            )
        }
    }

    override fun onDestroy() {
        importScope.cancel()
        authHttpClient.close()
        stockfishGateway.close()
        super.onDestroy()
    }
}
