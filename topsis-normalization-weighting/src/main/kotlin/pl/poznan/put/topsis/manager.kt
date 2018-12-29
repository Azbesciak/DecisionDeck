package pl.poznan.put.topsis

import pl.poznan.put.xmcda.ComputationManager

internal val weightedNormComputationManager = ComputationManager(
        NormalizationWeightingInputsHandler,
        NormalizationWeightingOutputsHandler
) {
    NormalizationWeightingCalculator(alternatives, criteria).calculate()
}