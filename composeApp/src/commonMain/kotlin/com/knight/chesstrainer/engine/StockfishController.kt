package com.knight.chesstrainer.engine

/** Platform processes own pipes and lifecycle; shared code owns UCI command semantics. */
interface UciTransport {
    suspend fun writeLine(command: String)
    suspend fun readLine(): String
    suspend fun close()
}

/** Platform host that owns the native engine process and keeps it off the UI thread. */
interface StockfishGateway {
    val available: Boolean
    suspend fun bestMove(fen: String, skillLevel: Int, depthCap: Int): String
    fun close()
}

class StockfishController(private val transport: UciTransport) {
    suspend fun initialize() {
        transport.writeLine("uci")
        readUntil("uciok")
        transport.writeLine("isready")
        readUntil("readyok")
    }

    suspend fun setStrength(skillLevel: Int, depthCap: Int) {
        require(skillLevel in 0..20)
        require(depthCap in 1..40)
        transport.writeLine("setoption name Skill Level value $skillLevel")
        transport.writeLine("setoption name UCI_LimitStrength value true")
        transport.writeLine("isready")
        readUntil("readyok")
        this.depthCap = depthCap
    }

    private var depthCap: Int = 12

    suspend fun bestMove(fen: String): String {
        require(fen.isNotBlank())
        transport.writeLine("position fen $fen")
        transport.writeLine("go depth $depthCap")
        while (true) {
            val line = transport.readLine().trim()
            if (line.startsWith("bestmove ")) return line.substringAfter("bestmove ").substringBefore(' ')
        }
    }

    suspend fun stop() {
        transport.writeLine("quit")
        transport.close()
    }

    private suspend fun readUntil(expected: String) {
        while (transport.readLine().trim() != expected) Unit
    }
}
