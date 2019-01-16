package pl.poznan.put.topsis

import pl.poznan.put.xmcda.OutputsHandler
import pl.poznan.put.xmcda.ranking.RankingParser

object DistanceRankingCalculatorOutputsHandler : OutputsHandler<TopsisRanking> {
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

    override fun convert(values: TopsisRanking) = mapOf(RANKING_NAME to RankingParser.convert(values))
}
