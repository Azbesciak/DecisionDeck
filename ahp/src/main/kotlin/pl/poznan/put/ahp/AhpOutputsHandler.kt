package pl.poznan.put.ahp

import pl.poznan.put.xmcda.OutputsHandler
import pl.poznan.put.xmcda.ranking.RankingParser

internal object AhpOutputsHandler : OutputsHandler<AhpRanking> {
    private const val RANKING_NAME = "scores"

    override fun xmcdaV3Tag(outputName: String) = when (outputName) {
        RANKING_NAME -> "alternativesValues"
        "messages" -> "programExecutionResult"
        else -> throw IllegalArgumentException("Unknown output name '$outputName'")
    }

    override fun xmcdaV2Tag(outputName: String) = when (outputName) {
        RANKING_NAME -> "alternativesValues"
        "messages" -> "methodMessages"
        else -> throw IllegalArgumentException("Unknown output name '$outputName'")
    }

    override fun convert(values: AhpRanking) =
            mapOf(RANKING_NAME to RankingParser.convert(values))
}
