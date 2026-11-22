package com.knight.chesstrainer.openings

import kotlinx.serialization.Serializable

@Serializable
data class RepertoirePly(
    val san: String,
    val uci: String,
    val guide: String = "",
)

@Serializable
data class OpeningRepertoire(
    val id: String,
    val opening: String,
    val variation: String,
    val summary: String,
    val plies: List<RepertoirePly>,
    val updatedAt: Long = 1L,
)

object OpeningCatalog {
    val starterLines = listOf(
        line("ruy-closed", "Morphy Defense · Closed", "Build a strong center, then prepare the d4 break.", listOf(
            "e2e4|e4|Claim the center and open the diagonal for your light-squared bishop.", "e7e5|e5", "g1f3|Nf3|Attack e5 and develop toward the center.", "b8c6|Nc6", "f1b5|Bb5|Pin the c6-knight to the king and pressure the e5-pawn.", "a7a6|a6", "b5a4|Ba4|Keep the bishop while preserving pressure on the diagonal.", "g8f6|Nf6", "e1g1|O-O|Castle before the center opens.", "f8e7|Be7", "f1e1|Re1|Support e4 and prepare a later d4 push.", "b7b5|b5", "a4b3|Bb3", "d7d6|d6", "c2c3|c3|Prepare d4 and give the bishop a retreat square.", "e8g8|O-O", "h2h3|h3|Prevent ...Bg4 and keep the kingside flexible.")),
        line("ruy-berlin", "Berlin Defense", "Meet the Berlin with early castling and a central d4 plan.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "g8f6|Nf6", "e1g1|O-O|Castle; Black now challenges the center.", "f6e4|Nxe4", "d2d4|d4|Open the center and gain time against the e4-knight.", "b7b5|b5", "a4b3|Bb3", "d7d5|d5", "d4e5|dxe5|Recover the knight and simplify into the Berlin endgame structure.", "c6e5|Nxe5")),
        line("ruy-exchange", "Exchange Variation", "Trade on c6, then use the bishop pair and a clear plan.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "a7a6|a6", "b5c6|Bxc6|Double Black's c-pawns and keep the bishop pair.", "d7c6|dxc6", "e1g1|O-O|Castle and prepare d4.", "f7f6|f6", "d2d4|d4|Challenge the center while Black's king is uncastled.", "e5d4|exd4", "f3d4|Nxd4|Recapture with a developed piece.", "c6c5|c5", "d4b3|Nb3|Retreat to safety and keep pressure on c5.")),
        line("ruy-schliemann", "Schliemann Defense", "Meet the sharp ...f5 gambit with development and a sound center.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "f7f5|f5", "b1c3|Nc3|Develop and add pressure to e4 before taking the pawn.", "f5e4|fxe4", "c3e4|Nxe4|Recapture with a developed knight.", "d7d5|d5", "f3e5|Nxe5|Remove the active c6-knight.", "d5e4|dxe4", "e5c6|Nxc6|Win the bishop pair and disrupt Black's queenside.")),
        line("ruy-classical", "Classical Defense", "Develop naturally against ...Bc5 and prepare the central break.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "f8c5|Bc5", "c2c3|c3|Prepare d4 and give the bishop room to retreat.", "g8f6|Nf6", "d2d4|d4|Challenge Black's center before it becomes stable.", "e5d4|exd4", "c3d4|cxd4|Open the c-file and establish a broad center.")),
        line("ruy-steinitz", "Steinitz Defense", "Use the space advantage and prepare a timely d4 break.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "d7d6|d6", "d2d4|d4|Use the extra space to challenge the center immediately.", "c8d7|Bd7", "b1c3|Nc3|Develop and reinforce the central squares.")),
        line("ruy-arkhangelsk", "Arkhangelsk Defense", "Develop the queenside bishop to b7 and contest the long diagonal.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "a7a6|a6", "b5a4|Ba4|Maintain the pin.", "b7b5|b5", "a4b3|Bb3", "c8b7|Bb7", "e1g1|O-O|Castle before opening the center.", "g8f6|Nf6", "d2d3|d3|Support e4 and keep the center closed until development is complete.", "f8c5|Bc5")),
        line("ruy-modern-arkhangelsk", "Modern Arkhangelsk", "Castle first, then meet ...Bb7 with a stable center.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "a7a6|a6", "b5a4|Ba4|Keep the bishop active.", "g8f6|Nf6", "e1g1|O-O|Castle and connect the rooks.", "b7b5|b5", "a4b3|Bb3", "c8b7|Bb7", "d2d3|d3|Support e4 and prepare c3 without blocking the bishop.")),
        line("ruy-cozio", "Cozio Defense", "Take space while Black develops the g8-knight by ...Ne7.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "c6e7|Ne7", "e1g1|O-O|Castle and prepare a central expansion.", "g7g6|g6", "d2d4|d4|Open the center while Black's kingside is still developing.")),
        line("ruy-bird", "Bird Defense", "Use the pin on c6 to challenge the ...Nd4 outpost.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "c6d4|Nd4", "f3d4|Nxd4|Trade the advanced knight and keep a healthy center.", "e5d4|exd4", "e1g1|O-O|Castle and prepare c3 to challenge the d4-pawn.")),
        line("ruy-norwegian", "Norwegian Defense", "Meet the early ...Na5 with castling and a central d4 plan.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "a7a6|a6", "b5a4|Ba4|Keep the bishop active.", "b7b5|b5", "a4b3|Bb3", "c6a5|Na5", "e1g1|O-O|Castle while Black's queenside knight is on the rim.", "a5b3|Nxb3", "a2b3|axb3|Recapture and preserve the bishop pair trade's material balance.", "d2d4|d4|Challenge the center.")),
        line("ruy-worrall", "Worrall Attack", "Use Qe2 to support e4 and keep a piece behind the center.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "a7a6|a6", "b5a4|Ba4|Maintain pressure on c6.", "g8f6|Nf6", "d1e2|Qe2|Support e4 and prepare to bring the rook to d1.", "b7b5|b5", "a4b3|Bb3", "d7d6|d6", "d2d3|d3|Reinforce the center and prepare a slower kingside development.")),
        line("ruy-smyslov", "Smyslov Defense", "Open the center against the fianchetto before Black completes development.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "g7g6|g6", "d2d4|d4|Challenge the center before Black's bishop reaches g7.", "e5d4|exd4", "f3d4|Nxd4|Recapture with development.", "f8g7|Bg7", "b1c3|Nc3|Support the center and develop the queenside.", "g8e7|Ne7", "c1e3|Be3|Develop and keep the d4-knight supported.", "e8g8|O-O", "d1d2|Qd2|Connect the rooks and prepare queenside castling or a kingside attack.", "d7d5|d5")),
        line("ruy-chigorin", "Chigorin Variation", "Complete the closed setup with Nbd2, Bc2, and a kingside regrouping.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "a7a6|a6", "b5a4|Ba4|Keep the bishop active.", "g8f6|Nf6", "e1g1|O-O|Castle before the center opens.", "f8e7|Be7", "f1e1|Re1|Support e4 and prepare the center.", "b7b5|b5", "a4b3|Bb3", "d7d6|d6", "c2c3|c3|Prepare the d4 break.", "e8g8|O-O", "h2h3|h3|Prevent ...Bg4 and give the king luft.", "c6b8|Nb8|The Chigorin regrouping prepares ...Nbd7.", "d2d4|d4|Take space while Black reorganizes.", "b8d7|Nbd7", "b1d2|Nbd2|Support e4 and prepare to reroute the knight.", "c8b7|Bb7", "b3c2|Bc2|Keep the bishop aimed at the kingside.", "f8e8|Re8", "d2f1|Nf1|Reroute toward g3 and h2.", "e7f8|Bf8", "f1g3|Ng3|Complete the regrouping and increase kingside pressure.")),
        line("ruy-zaitsev", "Zaitsev Variation", "Build the Zaitsev structure with Nbd2 and a timely a4 challenge.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "a7a6|a6", "b5a4|Ba4|Keep the bishop active.", "g8f6|Nf6", "e1g1|O-O|Castle before the center opens.", "f8e7|Be7", "f1e1|Re1|Support e4.", "b7b5|b5", "a4b3|Bb3", "d7d6|d6", "c2c3|c3|Prepare d4.", "e8g8|O-O", "h2h3|h3|Prevent ...Bg4.", "c8b7|Bb7", "d2d4|d4|Take space in the center.", "f8e8|Re8", "b1d2|Nbd2|Support e4 and prepare the kingside reroute.", "e7f8|Bf8", "a2a4|a4|Challenge Black's queenside pawn chain.")),
        line("ruy-marshall", "Marshall Attack", "Choose the principled d4 and active rook play against the Marshall gambit.", listOf(
            "e2e4|e4|Claim the center.", "e7e5|e5", "g1f3|Nf3|Attack e5.", "b8c6|Nc6", "f1b5|Bb5|Enter the Ruy Lopez.", "a7a6|a6", "b5a4|Ba4|Keep the bishop active.", "g8f6|Nf6", "e1g1|O-O|Castle before opening the center.", "f8e7|Be7", "f1e1|Re1|Support e4 and prepare d4.", "b7b5|b5", "a4b3|Bb3", "e8g8|O-O", "c2c3|c3|Prepare d4 and support the center.", "d7d5|d5", "d2d4|d4|The main move: challenge the center before Black strikes.", "e4d5|exd5|Accept the gambit and open the e-file.", "f6d5|Nxd5", "f3e5|Nxe5|Take the e5-pawn and force Black to recapture.", "c6e5|Nxe5", "e1e5|Rxe5|Recover the knight with the rook and keep active piece play.", "c7c6|c6", "e5e1|Re1|Retreat the rook to safety.", "e7d6|Bd6", "g2g3|g3|Challenge the queen and blunt the kingside attack.", "d8h4|Qh4", "d1f3|Qf3|Offer a queen trade and reduce Black's attacking force.")),
    )

    private fun line(id: String, variation: String, summary: String, tokens: List<String>): OpeningRepertoire {
        val plies = tokens.map { token ->
            val parts = token.split('|', limit = 3)
            RepertoirePly(parts[1], parts[0], parts.getOrElse(2) { "" })
        }
        return OpeningRepertoire(id, "Ruy Lopez", variation, summary, plies)
    }
}
