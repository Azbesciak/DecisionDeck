package pl.poznan.put.ahp

import org.xmcda.*
import pl.poznan.put.xmcda.OutputsHandler
import pl.poznan.put.xmcda.ranking.RankEntry
import pl.poznan.put.xmcda.ranking.Ranking

internal object AhpCriteriaOutputsHandler : OutputsHandler<AhpRanking> {
    private const val RANKING_NAME = "weights"

    override fun xmcdaV3Tag(outputName: String) = when (outputName) {
        RANKING_NAME -> "criteriaValues"
        "messages" -> "programExecutionResult"
        else -> throw IllegalArgumentException("Unknown output name '$outputName'")
    }

    override fun xmcdaV2Tag(outputName: String) = when (outputName) {
        RANKING_NAME -> "criteriaValues"
        "messages" -> "methodMessages"
        else -> throw IllegalArgumentException("Unknown output name '$outputName'")
    }

    override fun convert(values: AhpRanking) =
            mapOf(RANKING_NAME to CriteriaWeightsParser.convert(values))

    object CriteriaWeightsParser {
        fun convert(result: Ranking<*>): XMCDA {
            val criteriaWeights = CriteriaValues<Double>()
            criteriaWeights += result.map { it.toAssignment() }.toMap()
            return XMCDA().apply {
                criteriaValuesList += criteriaWeights
            }
        }

        private fun RankEntry<*>.toAssignment() =
                Criterion(alternative.name) to LabelledQValues<Double>(QualifiedValue(value))
    }
}
