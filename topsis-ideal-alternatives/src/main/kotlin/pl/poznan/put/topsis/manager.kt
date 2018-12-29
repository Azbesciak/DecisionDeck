package pl.poznan.put.topsis

import pl.poznan.put.xmcda.ComputationManager

internal val idealAlternativesComputationManager = ComputationManager(
        IdealAlternativesInputsHandler,
        IdealAlternativesOutputsHandler
) {
    IdealAlternativeCalculator(alternatives, criteria).calculate()
}