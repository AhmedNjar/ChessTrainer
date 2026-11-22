package com.knight.chesstrainer.puzzles

import com.knight.chesstrainer.db.ChessDatabase

class PuzzleRepository(private val database: ChessDatabase) {
    fun seedStarterIfEmpty(starter: List<PuzzleRecord> = StarterPuzzleSet.records): Long {
        if (totalPuzzleCount() == 0L) insertBatch(starter)
        return totalPuzzleCount()
    }

    suspend fun replaceFromCsv(lines: Sequence<String>, batchSize: Int = 500): Long {
        val iterator = lines.iterator()
        require(iterator.hasNext()) { "The selected CSV is empty" }
        val header = parsePuzzleCsvRow(iterator.next())
        require(header.take(3) == listOf("PuzzleId", "FEN", "Moves")) { "This file does not look like a Lichess puzzle CSV" }
        database.puzzleQueries.clearPuzzles()
        val rows = sequence { while (iterator.hasNext()) yield(iterator.next()) }
        return PuzzleCsvImporter { batch ->
            insertBatch(batch)
        }.import(rows, batchSize)
    }

    private fun insertBatch(batch: List<PuzzleRecord>) {
        database.transaction {
            batch.forEach { puzzle ->
                database.puzzleQueries.insertPuzzle(
                    puzzle.puzzleId, puzzle.fen, puzzle.moves, puzzle.rating.toLong(), puzzle.ratingDeviation.toLong(),
                    puzzle.popularity.toLong(), puzzle.nbPlays.toLong(), puzzle.themes, puzzle.gameUrl, puzzle.openingTags,
                    puzzle.dailyDate,
                )
            }
        }
    }

    fun totalPuzzleCount(): Long = database.puzzleQueries.countPuzzles().executeAsOne()

    fun findByRating(minimum: Long, maximum: Long, theme: String = "", limit: Long = 100) =
        database.puzzleQueries.filterPuzzles(minimum, maximum, theme, theme, limit).executeAsList()
}
