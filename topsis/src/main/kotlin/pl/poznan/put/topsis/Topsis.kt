package pl.poznan.put.topsis

enum class CriteriaType {
    COST, PROFIT
}

data class Criterion(val name: String, val weight: Double, val type: CriteriaType)
data class Alternative(val name: String, val criteriaValues: Map<Criterion, Int>)

data class RankEntry(val alternative: Alternative, val value: Double) {
    override fun toString() = "${alternative.name} - $value"
}

class Ranking private constructor(
        private val ranking: List<RankEntry>
) : List<RankEntry> by ranking {
    constructor(alternatives: List<Alternative>, coef: List<Double>) : this(
            alternatives.zip(coef)
                    .asSequence()
                    .sortedByDescending { it.second }
                    .map { RankEntry(it.first, it.second) }
                    .toList()
    )
}

class Topsis(private val alternatives: List<Alternative>, private val criteria: List<Criterion>) {
    private val weights: DoubleArray = criteria.map { it.weight }.toDoubleArray()
    private val decisionMatrix: Array<DoubleArray> = alternatives.map { alt ->
        criteria.map { cr ->
            val value = alt.criteriaValues[cr] ?: throw NullPointerException(
                    "value for criterion ${cr.name} not found for alternative ${alt.name}"
            )
            if (cr.type == CriteriaType.COST) -value.toDouble()
            else value.toDouble()
        }.toDoubleArray()
    }.toTypedArray()
    private val alternativeNo: Int = alternatives.size
    private val criteriaNo: Int = alternatives[0].criteriaValues.size

    fun calculate(): Ranking {
        val normDecMat = calculateNormalizedDecisionMatrix()
        val weighNormDecMat = calculateWeightedNormalizedDecisionMatrix(normDecMat)
        val posIdealSol = calculateIdealSolution(weighNormDecMat) { a, b -> a > b }
        val negIdealSol = calculateIdealSolution(weighNormDecMat) { a, b -> a < b }
        val distToPosIdealSol = calculateDistanceToIdealSolution(weighNormDecMat, posIdealSol)
        val distToNegIdealSol = calculateDistanceToIdealSolution(weighNormDecMat, negIdealSol)
        val coef = calculateClosenessCoefficient(distToPosIdealSol, distToNegIdealSol)
        return Ranking(alternatives, coef)
    }

    private fun calculateNormalizedDecisionMatrix(): Array<DoubleArray> {
        val sumPowSqrt = DoubleArray(criteriaNo)
        val normDecMat = Array(alternativeNo) { DoubleArray(criteriaNo) }
        for (col in 0 until criteriaNo) {
            var sumPow = 0.0
            for (row in 0 until alternativeNo) {
                sumPow += Math.pow(decisionMatrix[row][col], 2.0)
            }
            sumPowSqrt[col] = Math.sqrt(sumPow)
            for (row in decisionMatrix.indices) {
                normDecMat[row][col] = decisionMatrix[row][col] / sumPowSqrt[col]
            }
        }
        return normDecMat
    }

    private fun calculateWeightedNormalizedDecisionMatrix(normalizedDecisionMatrix: Array<DoubleArray>): Array<DoubleArray> {
        val weightedNormalizedDecisionMatrix = Array(alternativeNo) { DoubleArray(criteriaNo) }
        for (col in 0 until criteriaNo) {
            for (row in 0 until alternativeNo) {
                weightedNormalizedDecisionMatrix[row][col] = normalizedDecisionMatrix[row][col] * weights[col]
            }
        }
        return weightedNormalizedDecisionMatrix
    }

    private inline fun calculateIdealSolution(
            weightedNormalizedDecisionMatrix: Array<DoubleArray>,
            comp: (v1: Double, v2: Double) -> Boolean
    ): DoubleArray {
        val idealSolution = DoubleArray(criteriaNo)
        for (col in 0 until criteriaNo) {
            var best: Double? = null
            for (row in 0 until alternativeNo) {
                val valueForAlternative = weightedNormalizedDecisionMatrix[row][col]
                if (best == null || comp(valueForAlternative, best)) {
                    best = valueForAlternative
                    idealSolution[col] = best
                }
            }
        }
        return idealSolution
    }

    private fun calculateDistanceToIdealSolution(
            weightedNormDecMat: Array<DoubleArray>,
            idealSol: DoubleArray): DoubleArray {
        val distanceToIdealSol = DoubleArray(alternativeNo)
        val temp = DoubleArray(alternativeNo)
        for (row in 0 until alternativeNo) {
            for (col in 0 until criteriaNo) {
                temp[row] += Math.pow(weightedNormDecMat[row][col] - idealSol[col], 2.0)
            }
            distanceToIdealSol[row] = Math.sqrt(temp[row])
        }
        return distanceToIdealSol
    }

    private fun calculateClosenessCoefficient(distToPosIdealSol: DoubleArray, distToNegIdealSol: DoubleArray) =
            distToNegIdealSol
                    .zip(distToPosIdealSol)
                    .map { (neg, pos) -> neg / (neg + pos) }
}