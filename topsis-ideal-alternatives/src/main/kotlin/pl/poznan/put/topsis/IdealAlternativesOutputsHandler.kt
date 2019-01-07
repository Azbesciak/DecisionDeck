package pl.poznan.put.topsis

import pl.poznan.put.xmcda.OutputsHandler

object IdealAlternativesOutputsHandler : OutputsHandler<IdealAlternatives> {
    override fun xmcdaV3Tag(outputName: String) = when (outputName) {
        POSITIVE_IDEAL -> IDEAL_ALTERNATIVE_TAG
        NEGATIVE_IDEAL -> IDEAL_ALTERNATIVE_TAG
        "messages" -> "programExecutionResult"
        else -> throw IllegalArgumentException("Unknown output name '$outputName'")
    }

    override fun xmcdaV2Tag(outputName: String) = when (outputName) {
        POSITIVE_IDEAL -> IDEAL_ALTERNATIVE_TAG
        NEGATIVE_IDEAL -> IDEAL_ALTERNATIVE_TAG
        "messages" -> "methodMessages"
        else -> throw IllegalArgumentException("Unknown output name '$outputName'")
    }

    override fun convert(values: IdealAlternatives) = mapOf(
            POSITIVE_IDEAL to values.positive.asPerformance(),
            NEGATIVE_IDEAL to values.negative.asPerformance()
    )
}
