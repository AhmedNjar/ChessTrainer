package com.knight.chesstrainer.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/** Runs the bundled ARM64 Stockfish UCI executable as a persistent, private app process. */
class AndroidStockfishGateway(context: Context) : StockfishGateway {
    private val nativeLibraryDir = context.applicationInfo.nativeLibraryDir
    override val available: Boolean get() = java.io.File("$nativeLibraryDir/libstockfish.so").canExecute()
    private val mutex = Mutex()
    private var transport: ProcessUciTransport? = null
    private var controller: StockfishController? = null

    override suspend fun bestMove(fen: String, skillLevel: Int, depthCap: Int): String = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                val engine = controller ?: startEngine().also { controller = it }
                engine.setStrength(skillLevel.coerceIn(0, 20), depthCap.coerceIn(1, 40))
                engine.bestMove(fen)
            } catch (error: Throwable) {
                transport?.close()
                transport = null
                controller = null
                throw error
            }
        }
    }

    private suspend fun startEngine(): StockfishController {
        val executable = "$nativeLibraryDir/libstockfish.so"
        val process = ProcessBuilder(executable)
            .directory(java.io.File(nativeLibraryDir))
            .redirectErrorStream(true)
            .start()
        return StockfishController(ProcessUciTransport(process).also { transport = it }).also { it.initialize() }
    }

    override fun close() {
        transport?.terminate()
        transport = null
        controller = null
    }
}

private class ProcessUciTransport(private val process: Process) : UciTransport {
    private val reader = BufferedReader(InputStreamReader(process.inputStream))
    private val writer = BufferedWriter(OutputStreamWriter(process.outputStream))
    private val recentOutput = ArrayDeque<String>()

    override suspend fun writeLine(command: String) {
        writer.write(command)
        writer.newLine()
        writer.flush()
    }

    override suspend fun readLine(): String {
        val line = reader.readLine() ?: error(
            "Stockfish stopped before returning a UCI response (exit ${process.exitValueOrNull()}). " +
                recentOutput.takeLast(6).joinToString(" | ").takeIf(String::isNotBlank).orEmpty(),
        )
        recentOutput.addLast(line)
        if (recentOutput.size > 12) recentOutput.removeFirst()
        return line
    }

    override suspend fun close() {
        terminate()
    }

    fun terminate() {
        runCatching { writer.write("quit\n"); writer.flush() }
        runCatching { process.destroy() }
        runCatching { reader.close() }
        runCatching { writer.close() }
    }
}

private fun Process.exitValueOrNull(): Int? = try { exitValue() } catch (_: IllegalThreadStateException) { null }
