package pl.poznan.put.topsis

import pl.poznan.put.xmcda.OutputsHandler
import pl.poznan.put.xmcda.ranking.RankingParser

object DistanceRankingCalculatorOutputsHandler : OutputsHandler<TopsisRanking> {
    override fun xmcdaV3Tag(outputName: String) = when (outputName) {
        "ranking" -> "alternativesAssignments"
        "messages" -> "programExecutionResult"
        else -> throw IllegalArgumentException("Unknown output name '$outputName'")
    }

    override fun xmcdaV2Tag(outputName: String) = when (outputName) {
        "ranking" -> "alternativesAffectations"
        "messages" -> "methodMessages"
        else -> throw IllegalArgumentException("Unknown output name '$outputName'")
    }
    override fun convert(values: TopsisRanking) = mapOf("ranking" to RankingParser.convert(values))
}
