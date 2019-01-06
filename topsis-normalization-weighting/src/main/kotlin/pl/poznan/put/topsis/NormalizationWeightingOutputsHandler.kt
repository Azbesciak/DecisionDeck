package pl.poznan.put.topsis

import pl.poznan.put.xmcda.OutputsHandler

object NormalizationWeightingOutputsHandler : OutputsHandler<WeightedNormalizedAlternatives> {
    override fun xmcdaV3Tag(outputName: String) = when (outputName) {
        PERFORMANCE_NAME -> PERFORMANCE_TAG
        "messages" -> "programExecutionResult"
        else -> throw IllegalArgumentException("Unknown output name '$outputName'")
    }

    override fun xmcdaV2Tag(outputName: String) = when (outputName) {
        PERFORMANCE_NAME -> PERFORMANCE_TAG
        "messages" -> "methodMessages"
        else -> throw IllegalArgumentException("Unknown output name '$outputName'")
    }

    override fun convert(values: WeightedNormalizedAlternatives) = mapOf(PERFORMANCE_NAME to values.alternatives.asPerformance())
}
