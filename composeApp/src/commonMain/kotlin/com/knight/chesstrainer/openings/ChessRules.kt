package com.knight.chesstrainer.openings

import kotlin.math.abs

data class ChessMove(val from: String, val to: String, val promotion: Char? = null) {
    val uci: String get() = from + to + (promotion?.lowercaseChar()?.toString() ?: "")
}

data class ChessPosition(
    val pieces: Map<String, Char>,
    val whiteToMove: Boolean = true,
    val castling: Set<Char> = setOf('K', 'Q', 'k', 'q'),
    val enPassant: String? = null,
    val halfMove: Int = 0,
    val fullMove: Int = 1,
) {
    companion object {
        val initial = ChessPosition(
            pieces = mapOf(
                "a1" to 'R', "b1" to 'N', "c1" to 'B', "d1" to 'Q', "e1" to 'K', "f1" to 'B', "g1" to 'N', "h1" to 'R',
                "a2" to 'P', "b2" to 'P', "c2" to 'P', "d2" to 'P', "e2" to 'P', "f2" to 'P', "g2" to 'P', "h2" to 'P',
                "a7" to 'p', "b7" to 'p', "c7" to 'p', "d7" to 'p', "e7" to 'p', "f7" to 'p', "g7" to 'p', "h7" to 'p',
                "a8" to 'r', "b8" to 'n', "c8" to 'b', "d8" to 'q', "e8" to 'k', "f8" to 'b', "g8" to 'n', "h8" to 'r',
            )
        )
    }
}

object ChessRules {
    fun toFen(position: ChessPosition): String {
        val board = (8 downTo 1).joinToString("/") { rank ->
            var empty = 0
            buildString {
                for (file in 'a'..'h') {
                    val piece = position.pieces["$file$rank"]
                    if (piece == null) {
                        empty++
                    } else {
                        if (empty > 0) append(empty)
                        append(piece)
                        empty = 0
                    }
                }
                if (empty > 0) append(empty)
            }
        }
        val side = if (position.whiteToMove) "w" else "b"
        val castling = "KQkq".filter { it in position.castling }.ifEmpty { "-" }
        val ep = position.enPassant ?: "-"
        return "$board $side $castling $ep ${position.halfMove} ${position.fullMove}"
    }

    fun legalMoves(position: ChessPosition, from: String? = null): List<ChessMove> {
        val candidates = position.pieces.keys.asSequence()
            .filter { from == null || it == from }
            .filter { position.pieces[it]?.isUpperCase() == position.whiteToMove }
            .flatMap { square -> pseudoMoves(position, square).asSequence() }

        return candidates.filter { move ->
            val next = apply(position, move)
            val kingSquare = next.pieces.entries
                .firstOrNull { it.value == if (position.whiteToMove) 'K' else 'k' }
                ?.key
            kingSquare != null && !isSquareAttacked(next, kingSquare, byWhite = !position.whiteToMove)
        }.toList()
    }

    fun playUci(position: ChessPosition, uci: String): ChessPosition? {
        if (uci.length < 4) return null
        val from = uci.take(2)
        val to = uci.substring(2, 4)
        val promotion = uci.getOrNull(4)
        val move = legalMoves(position, from).firstOrNull {
            it.to == to && (promotion == null || it.promotion == promotion)
        }
        return move?.let { apply(position, it) }
    }

    fun apply(position: ChessPosition, move: ChessMove): ChessPosition {
        val movingPiece = position.pieces[move.from] ?: return position
        val nextPieces = position.pieces.toMutableMap()
        val captured = nextPieces[move.to]

        nextPieces.remove(move.from)

        // En passant capture
        if (movingPiece.lowercaseChar() == 'p' && move.to == position.enPassant && captured == null) {
            nextPieces.remove("${move.to[0]}${move.from[1]}")
        }

        // Promotion or normal move
        nextPieces[move.to] = move.promotion?.let {
            if (movingPiece.isUpperCase()) it.uppercaseChar() else it.lowercaseChar()
        } ?: movingPiece

        // Castling rook movement
        if (movingPiece.lowercaseChar() == 'k' && abs(file(move.to) - file(move.from)) == 2) {
            val rank = move.from[1]
            when (move.to[0]) {
                'g' -> {
                    nextPieces.remove("h$rank")
                    nextPieces["f$rank"] = if (movingPiece.isUpperCase()) 'R' else 'r'
                }
                'c' -> {
                    nextPieces.remove("a$rank")
                    nextPieces["d$rank"] = if (movingPiece.isUpperCase()) 'R' else 'r'
                }
            }
        }

        // Update castling rights
        val rights = position.castling.toMutableSet()
        when (movingPiece) {
            'K' -> { rights.remove('K'); rights.remove('Q') }
            'k' -> { rights.remove('k'); rights.remove('q') }
            'R' -> when (move.from) { "h1" -> rights.remove('K'); "a1" -> rights.remove('Q') }
            'r' -> when (move.from) { "h8" -> rights.remove('k'); "a8" -> rights.remove('q') }
        }
        when (move.to) {
            "h1" -> rights.remove('K'); "a1" -> rights.remove('Q')
            "h8" -> rights.remove('k'); "a8" -> rights.remove('q')
        }

        val nextEp = if (movingPiece.lowercaseChar() == 'p' && abs(rank(move.to) - rank(move.from)) == 2) {
            "${move.from[0]}${'1' + (rank(move.to) + rank(move.from)) / 2}"
        } else null

        val resetHalfMove = movingPiece.lowercaseChar() == 'p' || captured != null || move.to == position.enPassant

        return ChessPosition(
            pieces = nextPieces,
            whiteToMove = !position.whiteToMove,
            castling = rights,
            enPassant = nextEp,
            halfMove = if (resetHalfMove) 0 else position.halfMove + 1,
            fullMove = position.fullMove + if (!position.whiteToMove) 1 else 0,
        )
    }

    private fun pseudoMoves(position: ChessPosition, from: String): List<ChessMove> {
        val piece = position.pieces[from] ?: return emptyList()
        val white = piece.isUpperCase()
        val f = file(from)
        val r = rank(from)
        val result = mutableListOf<ChessMove>()

        fun add(targetFile: Int, targetRank: Int) {
            if (!inside(targetFile, targetRank)) return
            val target = square(targetFile, targetRank)
            val occupant = position.pieces[target]
            if (occupant == null || occupant.isUpperCase() != white) {
                result += ChessMove(from, target)
            }
        }

        fun slide(directions: List<Pair<Int, Int>>) {
            directions.forEach { (df, dr) ->
                var tf = f + df
                var tr = r + dr
                while (inside(tf, tr)) {
                    val target = square(tf, tr)
                    val occupant = position.pieces[target]
                    if (occupant == null) {
                        result += ChessMove(from, target)
                    } else {
                        if (occupant.isUpperCase() != white) {
                            result += ChessMove(from, target)
                        }
                        break
                    }
                    tf += df
                    tr += dr
                }
            }
        }

        when (piece.lowercaseChar()) {
            'p' -> {
                val dir = if (white) 1 else -1
                val one = r + dir
                if (inside(f, one) && position.pieces[square(f, one)] == null) {
                    val promo = if (one == (if (white) 7 else 0)) 'q' else null
                    result += ChessMove(from, square(f, one), promo)

                    val two = r + dir * 2
                    val startRank = if (white) 1 else 6
                    if (r == startRank && position.pieces[square(f, two)] == null) {
                        result += ChessMove(from, square(f, two))
                    }
                }
                for (df in listOf(-1, 1)) {
                    val tf = f + df
                    val tr = r + dir
                    if (!inside(tf, tr)) continue
                    val target = square(tf, tr)
                    val occupant = position.pieces[target]
                    if ((occupant != null && occupant.isUpperCase() != white) || target == position.enPassant) {
                        val promo = if (tr == (if (white) 7 else 0)) 'q' else null
                        result += ChessMove(from, target, promo)
                    }
                }
            }
            'n' -> listOf(
                1 to 2, 2 to 1, 2 to -1, 1 to -2,
                -1 to -2, -2 to -1, -2 to 1, -1 to 2
            ).forEach { add(f + it.first, r + it.second) }

            'b' -> slide(listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1))
            'r' -> slide(listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1))
            'q' -> slide(listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1, 1 to 0, -1 to 0, 0 to 1, 0 to -1))
            'k' -> {
                for (df in -1..1) {
                    for (dr in -1..1) {
                        if (df != 0 || dr != 0) add(f + df, r + dr)
                    }
                }
                val home = if (white) "e1" else "e8"
                val rankChar = if (white) '1' else '8'
                val kingRight = if (white) 'K' else 'k'
                val queenRight = if (white) 'Q' else 'q'
                if (from == home && !isSquareAttacked(position, home, !white)) {
                    if (kingRight in position.castling &&
                        position.pieces["f$rankChar"] == null &&
                        position.pieces["g$rankChar"] == null &&
                        position.pieces["h$rankChar"] == (if (white) 'R' else 'r') &&
                        !isSquareAttacked(position, "f$rankChar", !white)
                    ) {
                        result += ChessMove(from, "g$rankChar")
                    }
                    if (queenRight in position.castling &&
                        position.pieces["d$rankChar"] == null &&
                        position.pieces["c$rankChar"] == null &&
                        position.pieces["b$rankChar"] == null &&
                        position.pieces["a$rankChar"] == (if (white) 'R' else 'r') &&
                        !isSquareAttacked(position, "d$rankChar", !white)
                    ) {
                        result += ChessMove(from, "c$rankChar")
                    }
                }
            }
        }
        return result
    }

    private fun isSquareAttacked(position: ChessPosition, square: String, byWhite: Boolean): Boolean {
        val tf = file(square)
        val tr = rank(square)
        return position.pieces.any { (from, piece) ->
            if (piece.isUpperCase() != byWhite) return@any false
            val f = file(from)
            val r = rank(from)
            when (piece.lowercaseChar()) {
                'p' -> abs(tf - f) == 1 && tr - r == if (byWhite) 1 else -1
                'n' -> abs(tf - f) * abs(tr - r) == 2
                'k' -> maxOf(abs(tf - f), abs(tr - r)) == 1
                'b' -> abs(tf - f) == abs(tr - r) && clearRay(position, f, r, tf, tr)
                'r' -> (tf == f || tr == r) && clearRay(position, f, r, tf, tr)
                'q' -> (tf == f || tr == r || abs(tf - f) == abs(tr - r)) && clearRay(position, f, r, tf, tr)
                else -> false
            }
        }
    }

    private fun clearRay(position: ChessPosition, f: Int, r: Int, tf: Int, tr: Int): Boolean {
        val df = (tf - f).compareTo(0)
        val dr = (tr - r).compareTo(0)
        var x = f + df
        var y = r + dr
        while (x != tf || y != tr) {
            if (position.pieces.containsKey(square(x, y))) return false
            x += df
            y += dr
        }
        return true
    }

    private fun inside(file: Int, rank: Int) = file in 0..7 && rank in 0..7
    private fun file(square: String) = square[0] - 'a'
    private fun rank(square: String) = square[1] - '1'
    private fun square(file: Int, rank: Int) = "${'a' + file}${'1' + rank}"
}
