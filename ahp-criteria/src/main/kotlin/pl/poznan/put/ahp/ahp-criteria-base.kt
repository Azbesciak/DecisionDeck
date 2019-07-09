package pl.poznan.put.ahp

import pl.poznan.put.xmcda.ComputationManager

internal val ahpCriteriaComputationManager =
        ComputationManager(AhpCriteriaInputsHandler, AhpCriteriaOutputsHandler, AhpResult::compute)
