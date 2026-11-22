package com.knight.chesstrainer.puzzles

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

fun parsePuzzleCsvRow(line: String): List<String> {
    val values = mutableListOf<String>()
    val field = StringBuilder()
    var inQuotes = false
    var index = 0

    while (index < line.length) {
        val char = line[index]
        when {
            char == '"' && inQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                field.append('"')
                index++
            }
            char == '"' -> inQuotes = !inQuotes
            char == ',' && !inQuotes -> {
                values.add(field.toString())
                field.clear()
            }
            else -> field.append(char)
        }
        index++
    }
    values.add(field.toString())
    return values
}

fun parsePuzzleCsvRecord(line: String): PuzzleRecord? {
    val cols = parsePuzzleCsvRow(line)
    if (cols.size < 11 || cols[0] == "PuzzleId") return null
    return runCatching {
        PuzzleRecord(
            puzzleId = cols[0],
            fen = cols[1],
            moves = cols[2],
            rating = cols[3].toInt(),
            ratingDeviation = cols[4].toInt(),
            popularity = cols[5].toInt(),
            nbPlays = cols[6].toInt(),
            themes = cols[7],
            gameUrl = cols[8],
            openingTags = cols[9],
            dailyDate = cols[10].ifBlank { null },
        )
    }.getOrNull()
}

class PuzzleCsvImporter(private val insertBatch: suspend (List<PuzzleRecord>) -> Unit) {
    suspend fun import(lines: Sequence<String>, batchSize: Int = 500): Long {
        require(batchSize in 1..5000)
        var batch = ArrayList<PuzzleRecord>(batchSize)
        var importedCount = 0L
        val iterator = lines.iterator()

        while (iterator.hasNext()) {
            val line = iterator.next()
            parsePuzzleCsvRecord(line)?.let { record ->
                batch.add(record)
                if (batch.size == batchSize) {
                    insertBatch(batch)
                    importedCount += batch.size
                    batch = ArrayList(batchSize)
                }
            }
        }
        if (batch.isNotEmpty()) {
            insertBatch(batch)
            importedCount += batch.size
        }
        return importedCount
    }
}
