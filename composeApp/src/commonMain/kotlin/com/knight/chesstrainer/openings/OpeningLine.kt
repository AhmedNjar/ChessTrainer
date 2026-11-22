package com.knight.chesstrainer.openings

data class OpeningNode(val san: String, val children: MutableList<OpeningNode> = mutableListOf())

/** Small PGN move-text parser for repertoire files (comments and header tags are ignored). */
fun parsePgnMoveText(pgn: String): OpeningNode {
    val root = OpeningNode("START")
    val path = mutableListOf(root)
    val tokens = pgn.lineSequence()
        .filterNot { it.trim().startsWith("[") }
        .joinToString(" ")
        .replace(Regex("\\{[^}]*}"), " ")
        .replace(Regex(";[^\\n]*"), " ")
        .replace(Regex("\\([^)]*\\)"), " ")
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() && !it.matches(Regex("\\d+\\.(\\.\\.)?")) && it !in setOf("*", "1-0", "0-1", "1/2-1/2") }
    for (san in tokens) {
        val current = path.last()
        val next = current.children.firstOrNull { it.san == san } ?: OpeningNode(san).also(current.children::add)
        path += next
    }
    return root
}
