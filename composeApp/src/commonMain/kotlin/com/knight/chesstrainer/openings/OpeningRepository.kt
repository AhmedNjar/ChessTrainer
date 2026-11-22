package com.knight.chesstrainer.openings

import com.knight.chesstrainer.db.ChessDatabase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Stores downloaded openings on-device so selecting and training lines works without a network. */
class OpeningRepository(private val database: ChessDatabase) {
    private val json = Json { ignoreUnknownKeys = true }

    fun updateOfflineCatalog(): List<OpeningRepertoire> {
        save(OpeningCatalog.starterLines)
        return all()
    }

    fun save(lines: List<OpeningRepertoire>) {
        if (lines.isEmpty()) return
        database.transaction {
            lines.forEach { line ->
                database.puzzleQueries.insertOpeningRepertoire(
                    line.id, line.opening, line.variation, line.summary, json.encodeToString(line.plies), line.updatedAt,
                )
            }
        }
    }

    fun all(): List<OpeningRepertoire> = database.puzzleQueries.selectAllOpeningRepertoires().executeAsList().map { row ->
        OpeningRepertoire(
            id = row.id,
            opening = row.opening,
            variation = row.variation,
            summary = row.summary,
            plies = json.decodeFromString(row.pliesJson),
            updatedAt = row.updatedAt,
        )
    }
}
