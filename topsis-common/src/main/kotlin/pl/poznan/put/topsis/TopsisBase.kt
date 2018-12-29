package pl.poznan.put.topsis

import pl.poznan.put.xmcda.XmcdaFile

const val PERFORMANCE_NAME = "performance"
const val PERFORMANCE_TAG = "performanceTable"

val v3Base = arrayOf(
        XmcdaFile("alternatives"),
        XmcdaFile("criteria"),
        XmcdaFile(PERFORMANCE_NAME, tag = PERFORMANCE_TAG),
        XmcdaFile("weights", tag = "criteriaValues"),
        XmcdaFile("criteria_scales", tag = "criteriaScales")
)

val v2Base = arrayOf(
        XmcdaFile("alternatives"),
        XmcdaFile("criteria"),
        XmcdaFile(PERFORMANCE_NAME, tag = PERFORMANCE_TAG),
        XmcdaFile("weights", tag = "criteriaValues")
)

const val POSITIVE_IDEAL = "ideal_positive"
const val NEGATIVE_IDEAL = "ideal_negative"
const val IDEAL_ALTERNATIVE_TAG = PERFORMANCE_TAG
