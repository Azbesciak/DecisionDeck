package pl.poznan.put.topsis

import pl.poznan.put.xmcda.ranking.Alternative
import pl.poznan.put.xmcda.ranking.RankEntry
import pl.poznan.put.xmcda.ranking.Ranking
import kotlin.math.pow
import kotlin.math.sqrt

enum class CriteriaType {
    COST, PROFIT
}

data class Criterion(val name: String, val weight: Double, val type: CriteriaType)
data class TopsisAlternative(override val name: String, val criteriaValues: Map<Criterion, Double>) : Alternative

data class TopsisRankEntry(
        override val alternative: TopsisAlternative,
        override val value: Double
) : RankEntry<TopsisAlternative> {
    override fun toString() = "${alternative.name} - $value"
}

class TopsisRanking private constructor(
        private val ranking: List<TopsisRankEntry>
) : Ranking<TopsisAlternative>, List<RankEntry<TopsisAlternative>> by ranking {
    constructor(alternatives: List<TopsisAlternative>, coef: List<Double>) : this(
            alternatives.zip(coef)
                    .asSequence()
                    .sortedByDescending { it.second }
                    .map { TopsisRankEntry(it.first, it.second) }
                    .toList()
    )
}

class Topsis(private val alternatives: List<TopsisAlternative>, private val criteria: List<Criterion>) {
    private val weights: DoubleArray = criteria.map { it.weight }.toDoubleArray()
    private val decisionMatrix: Array<DoubleArray> = alternatives.map { alt ->
        criteria.map { cr ->
            val value = requireNotNull(alt.criteriaValues[cr]) {
                "value for criterion ${cr.name} not found for alternative ${alt.name}"
            }
            if (cr.type == CriteriaType.COST) -value
            else value
        }.toDoubleArray()
    }.toTypedArray()
    private val alternativeNo: Int = alternatives.size
    private val criteriaNo: Int = alternatives[0].criteriaValues.size

    fun calculate(): TopsisRanking {
        val normDecMat = calculateNormalizedDecisionMatrix()
        val weighNormDecMat = calculateWeightedNormalizedDecisionMatrix(normDecMat)
        val posIdealSol = calculateIdealSolution(weighNormDecMat) { a, b -> a > b }
        val negIdealSol = calculateIdealSolution(weighNormDecMat) { a, b -> a < b }
        val distToPosIdealSol = calculateDistanceToIdealSolution(weighNormDecMat, posIdealSol)
        val distToNegIdealSol = calculateDistanceToIdealSolution(weighNormDecMat, negIdealSol)
        val coef = calculateClosenessCoefficient(distToPosIdealSol, distToNegIdealSol)
        return TopsisRanking(alternatives, coef)
    }

    private fun calculateNormalizedDecisionMatrix(): Array<DoubleArray> {
        val normDecMat = Array(alternativeNo) { DoubleArray(criteriaNo) }
        (0 until criteriaNo).forEach { col ->
            var sumPow = 0.0
            (0 until alternativeNo).forEach { row ->
                sumPow += decisionMatrix[row][col].pow(2)
            }
            val sumPowSqrt = sqrt(sumPow)
            (0 until alternativeNo).forEach { row ->
                normDecMat[row][col] = decisionMatrix[row][col] / sumPowSqrt
            }
        }
        return normDecMat
    }

    private fun calculateWeightedNormalizedDecisionMatrix(normalizedDecisionMatrix: Array<DoubleArray>): Array<DoubleArray> {
        val weightedNormalizedDecisionMatrix = Array(alternativeNo) { DoubleArray(criteriaNo) }
        (0 until criteriaNo).forEach { col ->
            (0 until alternativeNo).forEach { row ->
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