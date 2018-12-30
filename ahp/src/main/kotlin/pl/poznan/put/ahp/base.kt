package pl.poznan.put.ahp

import pl.poznan.put.xmcda.ComputationManager
import pl.poznan.put.xmcda.XmcdaFile

internal val v2files = arrayOf(
        XmcdaFile("alternatives"),
        XmcdaFile("criteria"),
        XmcdaFile("hierarchy"),
        XmcdaFile("criteria_comparisons", tag = "criteriaComparisons"),
        XmcdaFile("preference", tag = "alternativesComparisons")
)

internal val v3files = arrayOf(
        XmcdaFile("alternatives"),
        XmcdaFile("criteria"),
        XmcdaFile("hierarchy", tag = "criteriaHierarchy"),
        XmcdaFile("criteria_comparisons", tag = "criteriaMatrix"),
        XmcdaFile("preference", tag = "alternativesMatrix")
)

internal val ahpComputationManager = ComputationManager(
        AhpInputsHandler, AhpOutputsHandler
) {
    require(invalidNode.isEmpty()) {
        "Found invalid nodes (cr should be below 0.1): ${invalidNode.joinToString(", ") { "${it.name} - ${it.cr}" }}}"
    }
    ranking
}

typealias CrytId = String
