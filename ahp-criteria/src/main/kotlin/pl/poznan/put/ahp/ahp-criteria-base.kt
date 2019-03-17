package pl.poznan.put.ahp

import pl.poznan.put.xmcda.ComputationManager
import pl.poznan.put.xmcda.XmcdaFile

internal val ahpCriteriaComputationManager = ComputationManager(
        AhpCriteriaInputsHandler, AhpCriteriaOutputsHandler
) {
    require(invalidNode.isEmpty()) {
        validityErrorMessage()
    }
    ranking
}
