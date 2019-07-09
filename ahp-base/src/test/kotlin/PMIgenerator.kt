import org.xmcda.*
import org.xmcda.converters.v2_v3.XMCDAConverter
import org.xmcda.parsers.xml.xmcda_v3.XMCDAParser
import org.xmcda.utils.Coord
import org.xmcda.utils.Matrix
import java.io.File

fun crt(id: String, name: String) = Criterion(id, name)

fun alt(id: String, name: String) = Alternative(id).also {
    it.setName(name)
}

infix fun <T, V> Pair<T, T>.has(ratio: V) = Triple(first, second, ratio)

fun Criterion.matrix(
        alternatives: List<Alternative>,
        vararg comparisons: Triple<Alternative, Alternative, Int>
): AlternativesMatrix<Int> {
    val childrenCount = alternatives.size
    validateSize(comparisons, childrenCount)
    return AlternativesMatrix<Int>().also {
        it.applyMatrixValues(this, comparisons) { id() }
    }
}

private fun validateSize(comparisons: Array<out Triple<*, *, Int>>, childrenCount: Int) {
    require(comparisons.size == (childrenCount * childrenCount - childrenCount) / 2) {
        "wrong values count"
    }
}

fun CriterionHierarchyNode.mat(
        vararg comparisons: Triple<CriterionHierarchyNode, CriterionHierarchyNode, Int>
): CriteriaMatrix<Int> =
        matrix(*comparisons.map { it.first.criterion to it.second.criterion has it.third }.toTypedArray())

fun CriterionHierarchyNode.matrix(
        vararg comparisons: Triple<Criterion, Criterion, Int>
): CriteriaMatrix<Int> {
    val childrenCount = children.size
    require(comparisons.size == (childrenCount * childrenCount - childrenCount) / 2) {
        "wrong values count"
    }
    return CriteriaMatrix<Int>().also {
        it.applyMatrixValues(criterion, comparisons) { id() }
    }
}

private fun <T, V : Comparable<V>> Matrix<T, V>.applyMatrixValues(criterion: Criterion, comparisons: Array<out Triple<T, T, V>>, id: T.() -> String) {
    setName(criterion.name())
    setId(criterion.id())
    comparisons.sortWith(compareBy({ it.first.id() }, { it.second.id() }, { it.third }))
    comparisons.toList()
            .windowed(2)
            .forEach { (c1, c2) ->
                require(c1.first != c2.first || c1.second != c2.second) {
                    "same comparison multiple times, [$c1, $c2]"
                }
            }
    comparisons.forEach {
        require(it.first != it.second) {
            "cannot repeat values: $it"
        }
    }
    comparisons.forEach { (c1, c2, value) ->
        set(Coord(c1, c2), QualifiedValues(QualifiedValue(value)))
    }
}


fun hierarchyNode(criterion: Criterion, vararg nodes: Criterion) =
        CriterionHierarchyNode(criterion).also {
            nodes.forEach { node -> it.addChild(node) }
        }

fun hierarchyNode(criterion: Criterion, vararg nodes: CriterionHierarchyNode) =
        CriterionHierarchyNode(criterion).also {
            nodes.forEach { node -> it.addChild(node) }
        }

fun <T> Criterion.matrix(alternatives: List<Alternative>, vararg value: T) where T : Number, T : Comparable<T> {
    require(value.size == alternatives.size * alternatives.size) {
        "wrong size"
    }
    val alts = alternatives
            .zip(value.toList().chunked(alternatives.size))
            .flatMap { (a1, vals) ->
                alternatives.zip(vals)
                        .map { (a2, v) -> a1 to a2 has v }
            }
    alts.map { }
}

fun main() {

    val teamCom = crt("teamCom", "Team Commitment")
    val orgCom = crt("orgCom", "Organizational Commitment")
    val pmCom = crt("pmCom", "Project Manager Commitment")
    val scCom = hierarchyNode(
            crt("scCom", "Stakeholders Commitment"),
            teamCom, orgCom, pmCom
    )
    val scComMat = scCom.matrix(
            teamCom to orgCom has 3,
            pmCom to teamCom has 5,
            pmCom to orgCom has 9
    )


    val roi = crt("roi", "Return on Investment")
    val profit = crt("profit", "Profit")
    val npv = crt("npv", "Net Present Value")
    val fin = hierarchyNode(
            crt("fin", "Financial"),
            roi, profit, npv
    )

    val finMat = fin.matrix(
            profit to roi has 5,
            npv to profit has 1,
            npv to roi has 5
    )

    val iacim = crt("iacim", "Imp. Ability to Compete in International Markets")
    val iip = crt("iip", "Imp. Internal Process")
    val ir = crt("ir", "Imp. Reputation")
    val str = hierarchyNode(
            crt("str", "Strategic"),
            iacim, iip, ir
    )

    val strMat = str.matrix(
            iacim to iip has 7,
            iacim to ir has 3,
            ir to iip has 5
    )

    val lr = crt("lr", "Lower Risk for the Organization")
    val ur = crt("ur", "Urgency")
    val itk = crt("itk", "Internal Technical Knowledge")
    val oc = hierarchyNode(
            crt("oc", "Other Criteria"),
            lr, ur, itk
    )

    val ocMat = oc.matrix(
            lr to ur has 5,
            itk to lr has 3,
            itk to ur has 7
    )

    val goal = hierarchyNode(
            crt("root", "Goal: ACME Project selection"),
            scCom, fin, str, oc
    )

    val goalMat = goal.mat(
            scCom to oc has 1,
            fin to scCom has 5,
            fin to str has 1,
            fin to oc has 5,
            str to scCom has 9,
            str to oc has 5
    )

    val nOffice = alt("no", "New Office")
    val erp = alt("erp", "ERP impl.")
    val chOff = alt("chOff", "Chinese Office")
    val iProd = alt("iProd", "Intern. Product")
    val itOuts = alt("itOuts", "IT Outsourc.")
    val locCamp = alt("locCamp", "Local Campaign")
    val alternatives = listOf(nOffice, erp, chOff, iProd, itOuts, locCamp)

    val teamComMat = teamCom.matrix(
            alternatives,
            nOffice to erp has 5,
            nOffice to chOff has 3,
            nOffice to itOuts has 9,
            nOffice to locCamp has 7,
            erp to itOuts has 1,
            chOff to erp has 5,
            chOff to itOuts has 7,
            chOff to locCamp has 3,
            iProd to nOffice has 3,
            iProd to erp has 7,
            iProd to chOff has 3,
            iProd to itOuts has 5,
            iProd to locCamp has 5,
            locCamp to erp has 3,
            locCamp to itOuts has 3
    )

    val orgComMat = orgCom.matrix(
            alternatives,
            nOffice to erp has 3,
            nOffice to itOuts has 5,
            nOffice to locCamp has 3,
            erp to itOuts has 1,
            chOff to nOffice has 9,
            chOff to erp has 9,
            chOff to iProd has 3,
            chOff to itOuts has 7,
            chOff to locCamp has 7,
            iProd to nOffice has 5,
            iProd to erp has 7,
            iProd to itOuts has 9,
            iProd to locCamp has 7,
            locCamp to erp has 3,
            locCamp to itOuts has 3
    )

    val pmComMat = pmCom.matrix(
            alternatives,
            nOffice to erp has 7,
            nOffice to itOuts has 5,
            nOffice to locCamp has 3,
            erp to itOuts has 3,
            chOff to nOffice has 3,
            chOff to erp has 9,
            chOff to iProd has 1,
            chOff to itOuts has 7,
            chOff to locCamp has 7,
            iProd to nOffice has 3,
            iProd to erp has 7,
            iProd to itOuts has 7,
            iProd to locCamp has 9,
            locCamp to erp has 3,
            locCamp to itOuts has 5
    )

    val roiMat = roi.matrix(
            alternatives,
            erp to nOffice has 3,
            chOff to nOffice has 7,
            chOff to erp has 9,
            chOff to itOuts has 7,
            chOff to locCamp has 5,
            iProd to nOffice has 9,
            iProd to erp has 9,
            iProd to chOff has 3,
            iProd to itOuts has 7,
            iProd to locCamp has 5,
            itOuts to nOffice has 3,
            itOuts to erp has 3,
            locCamp to nOffice has 3,
            locCamp to erp has 3,
            locCamp to itOuts has 3
    )
    val profitMat = profit.matrix(
            alternatives,
            nOffice to erp has 1,
            chOff to nOffice has 7,
            chOff to erp has 7,
            chOff to itOuts has 7,
            chOff to locCamp has 5,
            iProd to nOffice has 9,
            iProd to erp has 9,
            iProd to chOff has 3,
            iProd to itOuts has 9,
            iProd to locCamp has 5,
            itOuts to nOffice has 5,
            itOuts to erp has 3,
            locCamp to nOffice has 3,
            locCamp to erp has 5,
            locCamp to itOuts has 3
    )

    val npvMat = npv.matrix(
            alternatives,
            erp to nOffice has 3,
            erp to itOuts has 1,
            chOff to nOffice has 5,
            chOff to erp has 5,
            chOff to itOuts has 5,
            chOff to locCamp has 3,
            iProd to nOffice has 7,
            iProd to erp has 7,
            iProd to chOff has 3,
            iProd to itOuts has 5,
            iProd to locCamp has 7,
            itOuts to nOffice has 3,
            locCamp to nOffice has 3,
            locCamp to erp has 3,
            locCamp to itOuts has 3
    )

    val iacimMat = iacim.matrix(
            alternatives,
            nOffice to erp has 3,
            nOffice to itOuts has 5,
            nOffice to locCamp has 5,
            erp to locCamp has 3,
            chOff to nOffice has 9,
            chOff to erp has 9,
            chOff to iProd has 1,
            chOff to itOuts has 9,
            chOff to locCamp has 9,
            iProd to nOffice has 7,
            iProd to erp has 9,
            iProd to itOuts has 9,
            iProd to locCamp has 9,
            itOuts to erp has 3,
            itOuts to locCamp has 3
    )

    val iipMat = iip.matrix(
            alternatives,
            nOffice to chOff has 3,
            nOffice to iProd has 5,
            nOffice to itOuts has 1,
            nOffice to locCamp has 7,
            erp to nOffice has 5,
            erp to chOff has 7,
            erp to iProd has 7,
            erp to itOuts has 1,
            erp to locCamp has 7,
            chOff to iProd has 1,
            chOff to locCamp has 1,
            itOuts to chOff has 7,
            itOuts to iProd has 7,
            itOuts to locCamp has 7,
            locCamp to iProd has 3
    )

    val irMat = ir.matrix(
            alternatives,
            nOffice to itOuts has 3,
            erp to nOffice has 3,
            erp to itOuts has 5,
            chOff to nOffice has 7,
            chOff to erp has 9,
            chOff to iProd has 3,
            chOff to itOuts has 7,
            chOff to locCamp has 1,
            iProd to nOffice has 5,
            iProd to erp has 5,
            iProd to itOuts has 7,
            locCamp to nOffice has 7,
            locCamp to erp has 7,
            locCamp to iProd has 3,
            locCamp to itOuts has 9
    )

    val lrMat = lr.matrix(
            alternatives,
            nOffice to erp has 5,
            nOffice to chOff has 7,
            nOffice to iProd has 3,
            nOffice to itOuts has 5,
            nOffice to locCamp has 1,
            erp to chOff has 5,
            erp to iProd has 3,
            erp to itOuts has 3,
            iProd to chOff has 3,
            iProd to itOuts has 5,
            itOuts to chOff has 3,
            locCamp to erp has 7,
            locCamp to chOff has 9,
            locCamp to iProd has 7,
            locCamp to itOuts has 9
    )

    val urMat = ur.matrix(
            alternatives,
            nOffice to itOuts has 3,
            nOffice to locCamp has 1,
            erp to nOffice has 3,
            erp to itOuts has 3,
            erp to locCamp has 3,
            chOff to nOffice has 5,
            chOff to erp has 7,
            chOff to itOuts has 5,
            chOff to locCamp has 7,
            iProd to nOffice has 7,
            iProd to erp has 9,
            iProd to chOff has 3,
            iProd to itOuts has 7,
            iProd to locCamp has 7,
            locCamp to itOuts has 3
    )

    val itkMat = itk.matrix(
            alternatives,
            nOffice to erp has 9,
            nOffice to chOff has 9,
            nOffice to iProd has 9,
            nOffice to itOuts has 9,
            nOffice to locCamp has 3,
            chOff to erp has 3,
            chOff to iProd has 3,
            chOff to itOuts has 1,
            iProd to erp has 3,
            itOuts to erp has 5,
            itOuts to iProd has 3,
            locCamp to erp has 9,
            locCamp to chOff has 9,
            locCamp to iProd has 9,
            locCamp to itOuts has 9
    )
    val altXmcda = XMCDA().also {
        it.alternatives.addAll(alternatives)
    }
    val hierarchy = XMCDA().also {
        it.criteriaHierarchiesList.add(CriteriaHierarchy().apply { addRoot(goal) })
    }
    val criteria = XMCDA().also {
        val criteria = listOf(
                goal,

                scCom, fin, str, oc,

                teamCom, orgCom, pmCom,
                roi, profit, npv,
                iacim, iip, ir,
                lr, ur, itk
        ).map { if (it is CriterionHierarchyNode) it.criterion else it } as List<Criterion>
        it.criteria.addAll(criteria)
    }
    val criteriaComp = XMCDA().also {
        it.criteriaMatricesList.addAll(listOf(goalMat, scComMat, finMat, strMat, ocMat))
    }
    val altComp = XMCDA().also {
        it.alternativesMatricesList.addAll(
                listOf(
                        teamComMat, orgComMat, pmComMat,
                        roiMat, profitMat, npvMat,
                        iacimMat, iipMat, irMat,
                        lrMat, urMat, itkMat
                )
        )
    }

    val results = mapOf(
            "alternatives" to altXmcda,
            "criteria" to criteria,
            "criteria_comparisons" to criteriaComp,
            "hierarchy" to hierarchy,
            "preference" to altComp
    )
    val v2Mapping = mapOf(
            "alternatives" to "alternatives",
            "criteria" to "criteria",
            "criteria_comparisons" to "criteriaComparisons",
            "hierarchy" to "hierarchy",
            "preference" to "alternativesComparisons"
    )
    val v3Mapping = mapOf(
            "alternatives" to "alternatives",
            "criteria" to "criteria",
            "criteria_comparisons" to "criteriaMatrix",
            "hierarchy" to "criteriaHierarchy",
            "preference" to "alternativesMatrix"
    )
    writeV2("./ahp/tests/in4.v2", results, v2Mapping)
    writeV3("./ahp/tests/in4.v3", results, v3Mapping)
}

private fun writeV2(outFile: String, v3Results: Map<String, XMCDA>, tags: Map<String, String>) {
    val parent = File(outFile).also {
        it.deleteRecursively()
        it.mkdirs()
    }
    for (outputName in v3Results.keys) {

        val outputFile = File(parent, "$outputName.xml")
        val v2Results = requireNotNull(XMCDAConverter.convertTo_v2(v3Results[outputName])) {
            "Conversion from v3 to v2 returned a null value"
        }
        org.xmcda.parsers.xml.xmcda_v2.XMCDAParser.writeXMCDA(v2Results, outputFile, tags[outputName])
    }
}

private fun writeV3(outFile: String, x_results: Map<String, XMCDA>, tags: Map<String, String>) {
    val parser = XMCDAParser()
    val parent = File(outFile).also {
        it.deleteRecursively()
        it.mkdirs()
    }
    x_results.keys.forEach { key ->
        val outputFile = File(parent, "$key.xml")
        parser.writeXMCDA(x_results[key], outputFile, tags[key])

    }
}
