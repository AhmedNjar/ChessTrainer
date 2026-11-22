package com.knight.chesstrainer.puzzles

/** One row from the Lichess puzzle CSV. Values stay textual where the source is textual. */
data class PuzzleRecord(
    val puzzleId: String,
    val fen: String,
    val moves: String,
    val rating: Int,
    val ratingDeviation: Int,
    val popularity: Int,
    val nbPlays: Int,
    val themes: String,
    val gameUrl: String,
    val openingTags: String,
    val dailyDate: String?,
)

/** RFC 4180 compatible row parser; accepts embedded commas and escaped quotes. */
fun parsePuzzleCsvRow(line: String): List<String> {
    val values = mutableListOf<String>()
    val field = StringBuilder()
    var quoted = false
    var index = 0
    while (index < line.length) {
        val ch = line[index]
        when {
            ch == '"' && quoted && index + 1 < line.length && line[index + 1] == '"' -> {
                field.append('"'); index++
            }
            ch == '"' -> quoted = !quoted
            ch == ',' && !quoted -> { values += field.toString(); field.clear() }
            else -> field.append(ch)
        }
        index++
    }
    values += field.toString()
    return values
}

fun parsePuzzleCsvRecord(line: String): PuzzleRecord? {
    val c = parsePuzzleCsvRow(line)
    if (c.size < 11 || c[0] == "PuzzleId") return null
    return runCatching {
        PuzzleRecord(
            puzzleId = c[0], fen = c[1], moves = c[2], rating = c[3].toInt(),
            ratingDeviation = c[4].toInt(), popularity = c[5].toInt(), nbPlays = c[6].toInt(),
            themes = c[7], gameUrl = c[8], openingTags = c[9], dailyDate = c[10].ifBlank { null },
        )
    }.getOrNull()
}

/** Feed lines from a file stream; this never loads the multi-gigabyte source into memory. */
class PuzzleCsvImporter(private val insertBatch: suspend (List<PuzzleRecord>) -> Unit) {
    suspend fun import(lines: Sequence<String>, batchSize: Int = 500): Long {
        require(batchSize in 1..5000)
        var batch = ArrayList<PuzzleRecord>(batchSize)
        var imported = 0L
        val iterator = lines.iterator()
        while (iterator.hasNext()) {
            val line = iterator.next()
            parsePuzzleCsvRecord(line)?.let { record ->
                batch += record
                if (batch.size == batchSize) {
                    insertBatch(batch)
                    imported += batch.size
                    batch = ArrayList(batchSize)
                }
            }
        }
        if (batch.isNotEmpty()) {
            insertBatch(batch)
            imported += batch.size
        }
        return imported
    }
}
