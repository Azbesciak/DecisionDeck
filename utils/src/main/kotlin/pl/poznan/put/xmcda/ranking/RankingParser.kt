package pl.poznan.put.xmcda.ranking

import org.xmcda.*
import org.xmcda.Alternative


object RankingParser {
    fun convert(result: Ranking<*>): XMCDA {
        val alternativeAssignments = AlternativesValues<Double>()
        alternativeAssignments += result.map { it.toAssignment() }.toMap()
        return XMCDA().apply {
            alternativesValuesList += alternativeAssignments
        }
    }

    private fun RankEntry<*>.toAssignment() =
            Alternative(alternative.name) to LabelledQValues<Double>(QualifiedValue(value))
}
