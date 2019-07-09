package pl.poznan.put.ahp

import pl.poznan.put.xmcda.WarnException
import pl.poznan.put.xmcda.XmcdaFile

val criteriaV2files = arrayOf(
        XmcdaFile("criteria"),
        XmcdaFile("criteria_comparisons", tag = "criteriaComparisons")
)

val criteriaV3files = arrayOf(
        XmcdaFile("criteria"),
        XmcdaFile("criteria_comparisons", tag = "criteriaMatrix")
)

typealias CrytId = String

fun AhpResult.validityErrorMessage() =
        "Found invalid nodes (cr should be below ${Category.VALIDITY_THRESHOLD}): ${invalidNode.joinToString(", ") { "${it.name} - ${it.cr}" }}}"

fun AhpResult.compute() =
        if (invalidNode.isNotEmpty())
            throw WarnException(validityErrorMessage(), ranking)
        else
            ranking
