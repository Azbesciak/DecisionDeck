package pl.poznan.put.topsis

import pl.poznan.put.xmcda.ComputationManager
import pl.poznan.put.xmcda.XmcdaFile

internal val distanceCalcComputationManager = ComputationManager(
        DistanceRankingCalculatorInputsHandler,
        DistanceRankingCalculatorOutputsHandler
) {
    DistanceRankingCalculator(alternatives, criteria, idealAlternatives).calculate()
}

internal val idealAlts = arrayOf(
        XmcdaFile(POSITIVE_IDEAL, tag = IDEAL_ALTERNATIVE_TAG),
        XmcdaFile(NEGATIVE_IDEAL, tag = IDEAL_ALTERNATIVE_TAG)
)