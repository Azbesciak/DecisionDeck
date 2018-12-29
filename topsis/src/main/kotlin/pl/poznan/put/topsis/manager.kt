package pl.poznan.put.topsis

import pl.poznan.put.xmcda.ComputationManager

internal val topsisComputationManager = ComputationManager(
        singleInputTopsisHandler, TopsisOutputsHandler
) {
    Topsis(alternatives, criteria).calculate()
}