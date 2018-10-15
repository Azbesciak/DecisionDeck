package pl.poznan.put.ahp

import org.xmcda.*
import org.xmcda.Alternative


object RankingParser {
    fun convert(result: Ranking): XMCDA {
        val alternativeAssignments = AlternativesAssignments<Double>()
        alternativeAssignments += result.map { it.toAssignment() }
        return XMCDA().apply {
            alternativesAssignmentsList += alternativeAssignments
        }
    }

    private fun RankingPosition.toAssignment() =
            AlternativeAssignment<Double>().apply {
                alternative = Alternative(alternativeId)
                values = QualifiedValues<Double>(QualifiedValue(value))
            }
}
