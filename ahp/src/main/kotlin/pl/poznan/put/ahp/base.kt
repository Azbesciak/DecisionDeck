package pl.poznan.put.ahp

import pl.poznan.put.xmcda.ComputationManager
import pl.poznan.put.xmcda.XmcdaFile

internal val v2files = criteriaV2files + arrayOf(
        XmcdaFile("alternatives"),
        XmcdaFile("hierarchy"),
        XmcdaFile("preference", tag = "alternativesComparisons")
)

internal val v3files = criteriaV3files + arrayOf(
        XmcdaFile("alternatives"),
        XmcdaFile("hierarchy", tag = "criteriaHierarchy"),
        XmcdaFile("preference", tag = "alternativesMatrix")
)

internal val ahpComputationManager =
        ComputationManager(AhpInputsHandler, AhpOutputsHandler, AhpResult::compute)

typealias CrytId = String
