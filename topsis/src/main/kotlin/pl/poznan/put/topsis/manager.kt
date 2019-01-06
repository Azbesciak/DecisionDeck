package pl.poznan.put.topsis

import pl.poznan.put.xmcda.ComputationManager

internal val topsisComputationManager = ComputationManager(
        singleInputTopsisHandler(CriteriaWeightsFactory), TopsisOutputsHandler
) {
    Topsis(alternatives, criteria).calculate()
}