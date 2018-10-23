package pl.poznan.put.xmcda.ranking

import org.xmcda.*
import org.xmcda.Alternative


object RankingParser {
    fun convert(result: Ranking<*>): XMCDA {
        val alternativeAssignments = AlternativesAssignments<Double>()
        alternativeAssignments += result.map { it.toAssignment() }
        return XMCDA().apply {
            alternativesAssignmentsList += alternativeAssignments
        }
    }

    private fun RankEntry<*>.toAssignment() =
            AlternativeAssignment<Double>().apply {
                alternative = Alternative(this@toAssignment.alternative.name)
                values = QualifiedValues<Double>(QualifiedValue(value))
            }
}
